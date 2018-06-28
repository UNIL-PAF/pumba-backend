##########################################################################################
# Create mass fit
# 
#
##########################################################################################

# import the pumbaR library - you might have to build it
library(pumbaR)

# parameters
proteingroups_path <- "/Users/admin/Work/PAF/projects/SliceSILAC/latest/data/Conde_9508/proteinGroups.txt"
mass_fit_png <- "/tmp/mass_fit.png"

# load and fit the data
pg <- load_MQ(proteingroups_path)
mass_fit <- filter_and_fit(pg)

# create the image for the mass fit
png(file = mass_fit_png, width = 2400, height = 1200, units="px", pointsize = 12)
plot_fit(pg, mass_fit)
dev.off()
