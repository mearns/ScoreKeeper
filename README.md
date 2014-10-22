# ScoreKeeper
## *A polling agent for non-invasively extracting metrics and shipping them off to Statsd/Graphite*

We love Graphite. But we survived for many years without a purpose-driven metrics repository, wielding instead
an amalgam of monitoring tools, services and SQL Scripts for determining the health of our systems.

ScoreKeeper's job is to go out and collect these metrics and put them into Graphite, where they can be 
shared and correlated with real-time metrics.

The design philosophy is three-fold:
- Allow metrics to be configured quickly
Once you've configured your datasources and connections, SK's workflow is simple:
  1. Find a query, URL or JMX attribute that has a numeric answer you'd like to share or
  graph.
  2. Add a section to a configuration file, specifying 
    -a dotted metric name
    -a polling interval
    -the datasource to use
    -some details of how you intend to present the metric
  3. Restart SK to start reading the new metric (automatic change detection coming soon) 
- Be kind to engineers adding new metrics (so they will want to use SK)
  - We will try to make sane conventions and keep boilerplate short. 
  - We use HOCON, a configuration language that allows for greater human readability than JSON or XML. 
  - The triple-quote syntax allows big blocks of SQL to be copypasted into or out of
- Be kind to production systems (so nobody will demand you NOT use SK)
  - We work to have minimum impact on the sources of our metrics: avoiding locks, detuning over-aggressive schedules,
making efficient use of connections and network.
  - We use CircuitBreakers to avoid stressing systems that go offline as they come back on-line
  - Built using the Reactive paradigm on top of Akka (akka.io)
  
## Current metrics sources supported
### SQL queries
SQL queries are the main reason SK exists -- to avoid the pressure placed on systems when operators with magic queries
repeatedly jam F5 to see what's going on. SK will jam F5 for you on a sustainable schedule and Graphite will deliver
the results, cached, to data hungry operators.

ScoreKeeper is tested with Postgres and SQL Server but should work with any database that has a JDBC driver.

### JMX attributes
No monitoring app I've seen supports JMX well, and ScoreKeeper is no exception.

Note that we currently do not support JMX authentication and we realize that makes the app useless for many
applications.
### Site24x7
A source which records both an "up" value (0 or 1) and a response time (in ms) for each of the systems listed in a
URL. The metric name provided will be used as a prefix.

## Configuring SK
All ScoreKeeper configs are written in HOCON https://github.com/typesafehub/config/blob/master/HOCON.md
### system-props.conf
You describe the systems you're reading from and the systems you're writing to here.

### {monitoring subject}.conf
You describe the metrics you're collecting and the schedule here.

## Installing SK
Unzip.