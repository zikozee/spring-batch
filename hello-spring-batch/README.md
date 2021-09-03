    // task based step ::: procedure, copy a file, sending notification
    // chunk based step ::: ItemReader, ItemProcessor, ItemWriter
    
    
    //JobRepository maintains state of the jobs e.g start time , end time, status, items read or written by a job is written here
    //Job launcher :: starting the Job by calling a job.execute, validates job parameters or if job should run
    //Job executes steps

    // job parameters distinguish  a job from another

    // Step is a domain object that represents an independent, sequential, phase of a batch job

    //StepExecution represents a single attempt to execute a Step

    //ExecutionContext collection of key/value pairs controlled by the framewrk to allow developers a place to store
    // persistent state that is scoped to a stepexecution object or JobExecution Object
    // JobExecution >>> allows you pass values between steps


    //Chunk based step
    Item reader::: one item per time
    Item writer::  write batch or chunk per time

    they are independent of each other


    // JOB
    // if job parameters are the same, means you are creating a new jobExecution
    // however if you change parameters, it means you are creating a new job instance