# pumba-backend

This is the backaend for PUMBA written in Scala and R.
It works together with [pumbaR](https://github.com/UNIL-PAF/pumbaR) and [pumba-frontend](https://github.com/UNIL-PAF/pumba-frontend).

## Prerequisites
- R (version >= 4.0.4 )
- MongoDB (version <= 3.6.23)
- Java (version 1.8)
- sbt (version >= 1.5.3)

#### R packages
First you will need a couple of packages from CRAN:
- Rserve
- devtools

You will also have to install pumbaR from github:
```r
library(devtools)
install_github("UNIL-PAF/pumbaR", build_vignettes = TRUE)
```

#### Run development server
Start MongoDB from the command line:
`mongod --fork --dbpath $HOME/mongodb/db --logpath $HOME/mongodb/mongod.log`

Start the sbt server:
`sbt run`

#### Run tests
Start MongoDB from the command line:
`mongod --fork --dbpath $HOME/mongodb/db --logpath $HOME/mongodb/mongod.log`

Start tests:
`sbt test`


#### Deploy production server
`sbt clean dist`

This will create a new ZIP file in `target/universal/`.
You can then copy and unzip the file on your server and start it with:
`nohup ./bin/pumba -Dconfig.file=application.conf -Dhttp.port=9003 > logs/stdout.log 2> logs/stderr.log &`

You will have to adapt your `application.conf`.
