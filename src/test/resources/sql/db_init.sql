CREATE DATABASE IF NOT EXISTS fault_detector;

DROP TABLE IF EXISTS fault_detector.pre_aggregates;

CREATE TABLE fault_detector.pre_aggregates (
  timestamp Date,
  serviceId String,
  operationsCount Int32,
  runningOperationsCount Int32,
  overtimeOperationsCount Int32,
  errorOperationsCount Int32,
  successOperationsCount Int32,
  operationTimeLimit Int32,
  slidingWindow Int32,
  preAggregationSize Int32

) ENGINE MergeTree() PARTITION BY toYYYYMM(timestamp) ORDER BY (timestamp, serviceId) SETTINGS index_granularity=8192;


DROP TABLE IF EXISTS fault_detector.aggregates;

CREATE TABLE fault_detector.aggregates (
  timestamp Date,
  serviceId String,
  failureRate Float32,
  operationsCount Int64,
  successOperationsCount Int64,
  errorOperationsCount Int64,
  overtimeOperationsCount Int64

) ENGINE MergeTree() PARTITION BY toYYYYMM(timestamp) ORDER BY (timestamp, serviceId) SETTINGS index_granularity=8192;