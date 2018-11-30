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

# write the coefficients to a file (intercept, poly1, poly2, poly3)
cat(file = paste0(output_path, "/mass_fit_coeffs.csv"), sep=",", as.vector(mass_fit$coefficients))

# write fit predictions to another file
cat(file = paste0(output_path, "/mass_fits.csv"), sep=",", as.vector(predict(mass_fit, data.frame(variable = get_slice_numbers(pg)))))

# normalize intensities and write them to a new file
norm_pg <- get_normalized_table(pg)
write.table(norm_pg, file=paste0(output_path, "/normalizedProteinGroups.txt"), sep="\t", quote=FALSE, row.names=FALSE)

# write max normalized intensity to a file
max_norm_int <- get_max_norm_int(norm_pg)
cat(file = paste0(output_path, "/max_norm_intensity.csv"), sep=",", max_norm_int)

# create the image for the mass fit
print(paste0("create fit plot in [", mass_fit_png, "]."))
png(file = mass_fit_png, width = 2400, height = 1200, units="px", pointsize = 36)
plot_fit(pg, mass_fit)
dev.off()
