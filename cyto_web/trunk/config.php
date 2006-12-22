<?php

# Master Configuration File for Cytoscape.org Web Site
# Author:  Ethan Cerami, MSKCC

#error_reporting (E_ERROR | E_WARNING | E_PARSE | E_NOTICE);

################################################
#  Specifies the News Option for the Home Page
#  If set to "atom", we use the XML/Atom Feed from the Cytoscape Announce List.
#  If set to "static", we use the Static News Feed in news.php
################################################
$news_option = "atom";

#################################################
#  Production Setting
#  Specifies whether we are running in production on cytoscape.org.
#  If set to false, we assume a local test server set-up

#################################################
$in_production = true;

#################################################
#  Base URL(s)
#  Note:  as long as we keep the same paths, these should not be
#  updated.
#################################################
$cbio_base = "http://www.cbio.mskcc.org/cytoscape/release/";
$chianti_base = "http://chianti.ucsd.edu/";

#################################################
# Global Version Variables
# Note:  these should be updated with every release
# to update all refereces to the "latest" version,
# including release notes and download links
#################################################
$latest_version = "2.3.2";
$latest_release_notes_link = "cyto_2_3_features.php";
$latest_download_link = "download.php?file=cyto2_3_2"

#################################################
# Cytoscape 1.1 Release File Names
# Note:  these probably don't need to ever be updated.
#################################################
$cyto1_gz_east = $cbio_base."cytoscape-v1.1.1.tar.gz";
$cyto1_zip_east = $cbio_base."cytoscape-v1.1.1.zip";
$cyto1_source_east = $cbio_base."cytoscapeSource-v1.1.1.tar.gz";

#################################################
# Cytoscape 2.0 Release File Names
# Note:  these probably don't need to ever be updated.
#################################################
$cyto2_gz_east = $cbio_base."cytoscape-v2.00.tar.gz";
$cyto2_zip_east = $cbio_base."cytoscape-v2.00.zip";
$cyto2_source_east = $cbio_base."cytoscapeSource-v2.00.tar.gz";
$cyto2_mac_east = $cbio_base."cytoscape-v2.00.dmg.zip";

#################################################
# Cytoscape 2.1 Release File Names
#################################################
$cyto2_1_gz_east = $cbio_base."cytoscape-v2.1.tar.gz";
$cyto2_1_zip_east = $cbio_base."cytoscape-v2.1.zip";
$cyto2_1_source_east = $cbio_base."cytoscapeSource-v2.1.tar.gz";
$cyto2_1_mac_east = $cbio_base."cytoscape-v2.1.dmg.zip";

#################################################
# Cytoscape 2.2 Release File Names
#################################################
$cyto2_2_gz_east = $chianti_base."Cyto-2_2/cytoscape-v2.2.tar.gz";
$cyto2_2_zip_east = $chianti_base."Cyto-2_2/cytoscape-v2.2.zip";
$cyto2_2_source_east = $chianti_base."Cyto-2_2/cytoscapeSource-v2.2.tar.gz";
$cyto2_2_mac_east = $chianti_base."Cyto-2_2/Cytoscape-v2.2.dmg.zip";

#################################################
# Cytoscape 2.3 Release File Names
#################################################
$cyto2_3_gz_east = $chianti_base."Cyto-2_3/cytoscape-v2.3.tar.gz";
$cyto2_3_zip_east = $chianti_base."Cyto-2_3/cytoscape-v2.3.zip";
$cyto2_3_source_east = $chianti_base."Cyto-2_3/cytoscapeSource-v2.3.tar.gz";
$cyto2_3_mac_east = $chianti_base."Cyto-2_3/Cytoscape-v2.3.dmg.zip";

#################################################
# Cytoscape 2.3.1 Release File Names
#################################################
$cyto2_3_1_gz_east = $chianti_base."Cyto-2_3_1/cytoscape-v2.3.1.tar.gz";
$cyto2_3_1_zip_east = $chianti_base."Cyto-2_3_1/cytoscape-v2.3.1.zip";
$cyto2_3_1_source_east = $chianti_base."Cyto-2_3_1/cytoscapeSource-v2.3.1.tar.gz";
$cyto2_3_1_mac_east = $chianti_base."Cyto-2_3_1/Cytoscape-v2.3.1.dmg.zip";

#################################################
# Cytoscape 2.3.2 Release File Names
#################################################
$cyto2_3_2_gz_east = $chianti_base."Cyto-2_3_2/cytoscape-v2.3.2.tar.gz";
$cyto2_3_2_zip_east = $chianti_base."Cyto-2_3_2/cytoscape-v2.3.2.zip";
$cyto2_3_2_source_east = $chianti_base."Cyto-2_3_2/cytoscapeSource-v2.3.2.tar.gz";

$cyto2_3_2_mac = $chianti_base."Cyto-2_3_2/Cytoscape_2_3_2_macos.dmg";
$cyto2_3_2_windows = $chianti_base."Cyto-2_3_2/Cytoscape_2_3_2_windows.exe";
$cyto2_3_2_linux = $chianti_base."Cyto-2_3_2/Cytoscape_2_3_2_unix.sh";

#################################################
# Cytoscape 2.4 Release File Names
#################################################
$cyto2_4_gz_east = $chianti_base."Cyto-2_4/cytoscape-v2.4.tar.gz";
$cyto2_4_zip_east = $chianti_base."Cyto-2_4/cytoscape-v2.4.zip";
$cyto2_4_source_east = $chianti_base."Cyto-2_4/cytoscapeSource-v2.4.tar.gz";

$cyto2_4_mac = $chianti_base."Cyto-2_4/Cytoscape_2_4_macos.dmg";
$cyto2_4_windows = $chianti_base."Cyto-2_4/Cytoscape_2_4_windows.exe";
$cyto2_4_linux = $chianti_base."Cyto-2_4/Cytoscape_2_4_unix.sh";

#################################################
# URL for Cytoscape Install Anywhere
#################################################
$cyto1_install_anywhere = "http://db.systemsbiology.net:8080/cytoscape/download/installer/installer1.1.1/install.htm";
$cyto2_install_anywhere = "ftp://baker.systemsbiology.net/pub/xmas/cytoscape2.0/cytoscape2.0_Build_Output/Web_Installers/install.htm";
$cyto2_1_install_anywhere = "http://chianti.ucsd.edu/Cyto2.1/install.htm";

$cyto2_2_install_anywhere = "http://chianti.ucsd.edu/Cyto-2_2/install.htm";
$cyto2_3_install_anywhere = "http://chianti.ucsd.edu/Cyto-2_3/install.htm";
$cyto2_3_1_install_anywhere = "http://chianti.ucsd.edu/Cyto-2_3_1/install.htm";

##################################################
# Log file for Agilent Literature Search
##################################################

#  Production Settings
#  Do not Modify
if ($in_production == true) {
	#  The Real Cytoscape Data File
	$cyto_data = "/usr/local/www/virtual3/66/175/24/126/data/cyto_data.txt";
	$litsearch_log = "/usr/local/www/virtual3/66/175/24/126/data/litsearch_log.txt";
} else {
	#  A Test Data File (Used for local testing purposes only)
	$cyto_data = "data/cyto_data.txt";
	$litsearch_log = "data/litsearch_log.txt";
}
?>

