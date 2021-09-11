# spring-batch
# spring-batch
# spring-batch

Transient Exceptions: are those that when retried could succeed without changing anything
e.g Network Resource Unavailable, Database lock


Parallel Flow Job
=================

1. Group tasklet that depend on each other in the same flow

2. create a diff flow with the split attribute to add a aync process
then add flows that belong to 1 category above
3. we can then add the flow in the Job
4. the we add other flows or steps
5. then end 
6. and then build

PARTITIONING
==========
Useful when a large dataset from a table is to be read, processed and written somewhere
we use
JdbcPagingItemReader
RangePartitioner
and customer ColumnRangePartitioner

there will  several **slave jobs** (threads) 
partition is passed to the slaves to ("**read**, **process** and **write**")
