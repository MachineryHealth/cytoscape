# Syncs latest file changes to cytoscape.org
# - excludes all CVS directories

#  Configure for production
ant config_prod

#  Issue rsync command
rsync --verbose --progress --stats --compress --rsh=/usr/bin/ssh --recursive --times --perms --links --exclude "CVS/" --cvs-exclude --exclude "deploy.sh" --exclude "build.xml" . treyideker@cytoscape.org:/usr/local/www/virtual3/66/175/24/126/html/  
