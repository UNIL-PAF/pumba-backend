##########################################################################################
# Create mass fit
# 
#
##########################################################################################

# import the pumbaR library - you might have to build it
library(pumbaR)

# parse parameters from command line
args <- commandArgs(trailingOnly=TRUE)
if(length(args) != 2){
    stop("usage: create_mass_fit.R [proteinGroups.txt path] [output path]")
}

# "/Users/admin/Work/PAF/projects/SliceSILAC/latest/data/Conde_9508/proteinGroups.txt"
proteingroups_path <- args[1]
output_path <- args[2]
mass_fit_png <- paste0(output_path, "/mass_fit.png")

# load and fit the data
print(paste0("load data from [", proteingroups_path, "]."))
pg <- load_MQ(proteingroups_path)
print(paste0("dimensions: ", dim(pg)))
mass_fit <- filter_and_fit(pg)
save(file = paste0(output_path, "/mass_fit.RData"), mass_fit)

# create the image for the mass fit
print(paste0("create fit plot in [", mass_fit_png, "]."))
png(file = mass_fit_png, width = 2400, height = 1200, units="px", pointsize = 36)
plot_fit(pg, mass_fit)
dev.off()
