#!/usr/bin/env Rscript

##########################################################################################
# Create mass fit
# 
#
##########################################################################################

# import the pumbaR library - you might have to build it
library(pumbaR)

# parse parameters from command line
library(optparse)

option_list <- list( 
    make_option(c("-i", "--input"), dest="proteingroups_path", type="character",
        help="Path to proteinGroups.txt."),
    make_option(c("-o", "--output"), dest="output_path", type="character",
        help="Directory in which results should be stored."),
    make_option(c("--low-density-threshold"), dest="low_density_threshold", default=10, type="integer",
        help="Threshold at which low density values are ignored for mass-slice-fitting [default %default]."),
    make_option(c("--ignore-slice"), dest="ignore_slice", default=NULL, type="character",
        help="Slices that should be ignored for the parsing [default %default]."),
    make_option(c("--sample-name"), dest="sample_name", default=NULL, type="character",
        help="Sample name in case MaxQuant analysis was done with several samples [default %default].")
    )

opt <- parse_args(OptionParser(option_list=option_list))

if(! is.null(opt$ignore_slice)) ignore_slice <- as.numeric(strsplit(opt$ignore_slice, ",")[[1]]) else ignore_slice <- NULL

mass_fit_png <- paste0(opt$output_path, "/mass_fit_1.png")
mass_fit_png_2 <- paste0(opt$output_path, "/mass_fit_2.png")
mass_fit_png_3 <- paste0(opt$output_path, "/mass_fit_3.png")
mass_fit_png_4 <- paste0(opt$output_path, "/mass_fit_4.png")

png_width <- 1200
png_height <- 600
png_pointsize <- 36

print(opt$sample_name)

# load and fit the data
print(paste0("load data from [", opt$proteingroups_path, "]."))
pg <- load_MQ(opt$proteingroups_path, ignore_slice = ignore_slice, sample_name = opt$sample_name)
print(paste0("dimensions: ", dim(pg)))

# filter and fit
pg_2 <- filter_repeated_entries(pg)
pg_3 <- filter_weak_intensity(pg_2)
pg_4 <- filter_low_densities(pg_3, min_nr_threshold = opt$low_density_threshold)
mass_fit <- fit_curve(pg_4)

save(file = paste0(opt$output_path, "/mass_fit.RData"), mass_fit)

# write the coefficients to a file (intercept, poly1, poly2, poly3)
cat(file = paste0(opt$output_path, "/mass_fit_coeffs.csv"), sep=",", as.vector(mass_fit$coefficients))

# write fit predictions to another file
cat(file = paste0(opt$output_path, "/mass_fits.csv"), sep=",", as.vector(predict(mass_fit, data.frame(variable = get_slice_numbers(pg)))))

# normalize intensities and write them to a new file
norm_pg <- get_normalized_table(pg, sample_name = opt$sample_name)
write.table(norm_pg, file=paste0(opt$output_path, "/normalizedProteinGroups.txt"), sep="\t", quote=FALSE, row.names=FALSE)

# write max normalized intensity to a file
max_norm_int <- get_max_norm_int(norm_pg)
cat(file = paste0(opt$output_path, "/max_norm_intensity.csv"), sep=",", max_norm_int)

# write normalization correction factor to a file
norm_corr_factor <- get_correction_factor(get_intensities(pg, sample_name = opt$sample_name))
cat(file = paste0(opt$output_path, "/norm_corr_factor.csv"), sep=",", norm_corr_factor)

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

