##########################################################################################
# Create mass fit
# 
#
##########################################################################################

# import the pumbaR library - you might have to build it
library(pumbaR)

# parse parameters from command line
args <- commandArgs(trailingOnly=TRUE)
if(length(args) < 2){
    stop("usage: create_mass_fit.R [proteinGroups.txt path] [output path] [low_density_threshold (OPTIONAL)] [ignore_slices (OPTIONAL)]")
}

# "/Users/admin/Work/PAF/projects/SliceSILAC/latest/data/Conde_9508/proteinGroups.txt"
proteingroups_path <- args[1]
output_path <- args[2]
mass_fit_png <- paste0(output_path, "/mass_fit_1.png")
mass_fit_png_2 <- paste0(output_path, "/mass_fit_2.png")
mass_fit_png_3 <- paste0(output_path, "/mass_fit_3.png")
mass_fit_png_4 <- paste0(output_path, "/mass_fit_4.png")

low_density_threshold <- if(length(args) >= 3) as.numeric(args[3]) else 10
ignore_slices <- if(length(args) == 4) as.numeric(strsplit(args[4], ",")[[1]]) else NULL

png_width <- 1200
png_height <- 600
png_pointsize <- 36

# load and fit the data
print(paste0("load data from [", proteingroups_path, "]."))
pg <- load_MQ(proteingroups_path, ignore_slices)
print(paste0("dimensions: ", dim(pg)))

# filter and fit
pg_2 <- filter_repeated_entries(pg)
pg_3 <- filter_weak_intensity(pg_2)
pg_4 <- filter_low_densities(pg_3, min_nr_threshold = low_density_threshold)
mass_fit <- fit_curve(pg_4)

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

# create the images for the mass fit
print(paste0("create fit plot in [", mass_fit_png, "]."))
png(file = mass_fit_png, width = png_width, height = png_height, units="px", pointsize = png_pointsize)
plot_fit(pg, mass_fit)
dev.off()

png(file = mass_fit_png_2, width = png_width, height = png_height, units="px", pointsize = png_pointsize)
plot_fit(pg_2, mass_fit)
dev.off()

png(file = mass_fit_png_3, width = png_width, height = png_height, units="px", pointsize = png_pointsize)
plot_fit(pg_3, mass_fit)
dev.off()

png(file = mass_fit_png_4, width = png_width, height = png_height, units="px", pointsize = png_pointsize)
plot_fit(pg_4, mass_fit)
dev.off()

