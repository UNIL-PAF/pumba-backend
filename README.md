# pumba-backend [![Build Status](https://travis-ci.org/UNIL-PAF/pumba-backend.svg?branch=master)](https://travis-ci.org/UNIL-PAF/pumba-backend)

## Installation

### Prerequisites
- R (version > 2.???)
- MongoDB (version )
- Java (version)
- sbt (version)

### R packages
First you will need a couple of packages from CRAN:
- Rserve
- devtools

You will also have to install pumbaR from github:
`install_github("UNIL-PAF/pumbaR", build_vignettes = TRUE)` 

### Run the development server
Start MongoDB from the command line:
`mongod --fork --dbpath $HOME/mongodb/db --logpath $HOME/mongodb/mongod.log`

Start the sbt server:
`sbt run`