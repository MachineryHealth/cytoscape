<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<? include "config.php"; ?>

<! Updated by kono for 2.6 4_2_2008 >
<! Updated by apico with "latest release" variables for future updates (see config.php). 12_21_2006 >

<html>

	<head>
		<title>Cytoscape: Analyzing and Visualizing Network Data</title>
		<link href="css/cytoscape.css" type="text/css" rel="stylesheet" media="screen">
		<link href="images/cyto.ico" rel="shortcut icon">
		<meta name="keywords" content="Cytoscape, visualization, interaction network, 
		software, genetic, gene, expression, protein interaction, graph, bioinformatics, 
		computational biology, Whitehead Institute, Institute for Systems Biology, 
		microarray analysis, clustering, pathways, integration, algorithm, 
		simulated annealing, gene regulation, complex network">
	</head>

	<body>
	<div id="container">	
		<div id="feature">
			<div class="title">Cytoscape</div>
			<img src="images/logo.png">
			
			<div class="article">
				<h3><a href="<?= $latest_download_link ?>">Download Cytoscape 
				<?= $latest_version ?>!</a></h3>
				
				<a href="<?= $latest_release_notes_link ?>"><?= $latest_version ?> Release Notes &raquo; </a>
				
			</div>
		</div>
		
		<? include "nav.php"; ?>
		
		<? include "detailed_nav.php"; ?>

		
		<div id="main">

			<div class="item">
				<h2><i>New!</i> Cytoscape 2.6.0</h2>
				<a href="screenshots/2_6_ss1.png">
					<img src="screenshots/2_6_ss1_thumb.png" alt="Cytoscape 2.6 Screenshot" />
				</a>
				
				<div id="paragraph">
					(Updated 4/11/2008) New features include: <br>
					<ul id="paragraph">
						<li><strong>Web Service Client Manager</strong></li>
							<ul>
							<li>Seamless access to Pathway Commons, IntAct, and NCBI Entrez Gene.</li>
							<li>Synonym import from BioMart.</li>
							</ul>
						<li><strong>Cytoscape Themes</strong></li>
						<li><strong>Dynamic Filters</strong></li>
						<li><strong>Network Manager supports multiple network selection</strong></li>
						<li><strong>Label Positioning has been improved</strong></li>
						<li><strong>Session saving occurs in memory</strong></li>
						<li><strong>XGMML loading/saving optimized</strong></li>
						<li><strong>Linkout integrated with attribute browser</strong></li>
						<li><strong>Extra sample Visual Styles using new visual properties</strong></li>
						<li><strong>Many, many bug fixes!</strong></li>
					</ul>
					<a href="cyto_2_6_features.php">Cytoscape 2.6.0 release notes</a>
				</div>
				
			</div>


			<div class="item">
				<h2>Cytoscape 2.5.2</h2>
				<div id="paragraph">
					This is a bug-fix release that addresses issues related to XGMML 
					loading and parsing old vizmap.props files.  Enjoy!
				</div>
			</div>

			<div class="item">
				<h2>Cytoscape 2.5.1</h2>
				<div id="paragraph">
					A point release to address a variety of bugs in the 2.5.0 release.
				</div>
			</div>


			<div class="item">
				<h2>Cytoscape 2.5.0</h2>
				<a href="screenshots/2_5_ss1.png">
					<img src="screenshots/2_5_ss1_thumb.png" alt="Cytoscape 2.5 Screenshot" />
				</a>
				<div id="paragraph">
					(Updated 7/23/2007) New features include: <br>
					<ul id="paragraph">
						<li><strong>New VizMapper User Interface</strong></li>
						<ul>
						<li>More intuitive</li>
						<li>Continuous mapping editors</li>
						<li>Visual editor for default view</li>
						<li>Visual mapping browser</li>
						<li>Improved visual legend generator</li> 
						<li>Utilities to generate discrete values</li> 
						</ul>
						
						<li><strong>New Features for Visual Style</strong></li>
						<ul>
						<li>Transparency (opactiy) support</li>
						<li>Continuous edge width</li>
						<li>Color visual property is separated from Arrow and Edge</li>
						</ul>
						
						<li><strong>New Filter User Interface</strong></li>
						<ul>
						<li>Intuitive widgets for basic filters</li>		
						<li>Suggested search values with indexing</li>
						<li>Options to save in session or globally.
						</ul>
						
						<li><strong>Plugin Manager and New Plugin Website</strong></li>
						<ul>
						<li>Install/Update/Delete plugins from within Cytoscape</li>	
						<li>Search for version compatible plugins from any host site</li>
						<li>Display list of installed plugins</li>
						</ul>
						
						<li><strong>Layout customization</strong></li>
						<li><strong>Undo and Redo</strong></li>
						<li><strong>Group API for plugin developers</strong></li>
						<li><strong>Node stacking</strong></li>
						<li><strong>Tested on both Java SE 5 and 6</strong></li>
						<li><strong>Many, many bug fixes!</strong></li>
					</ul>
				</div>
			</div>
			<div class="item">
				<h2><a href="retreat2007/index.php">Cytoscape Retreat 2007!</a></h2>
				<a href="retreat2007/venue.php">
					<img src="retreat2007/images/magere-brug-small.jpg" alt="Amsterdam by night"/>
				</a>
				<div id="paragraph">
						Now in Europe! November 6<sup>th</sup> - 9<sup>th</sup><br>
					 Including a public symposium on November 8<sup>th</sup>, with a formidable list of confirmed speakers among them
					<ul id="paragraph">
						<li>Leroy Hood 	
						<li>Peter Sorger 
						<li>Ewan Birney 
					</ul>
					 Hosted by the <a href="http://www.humangenetics-amc.nl" target="_blank">Human Genetics Department of the Academic Medical Center</a> in the vibrant historic city of <a href="/retreat2007/venue.php">Amsterdam</a>. </div>
			</div>
			<div class="item">
				<h2>Cytoscape 2.4.1</h2>
				<div id="paragraph">
					No new features, but several bugs have been fixed. </div>
			</div>
			<div class="item">
				<h2>Cytoscape 2.4.0 </h2>
				<a href="screenshots/2_4_ss1.png"><img src="screenshots/2_4_ss1_thumb.png" alt="Cytoscape 2.4.0 Screenshot" align="left" border="0"> </a>
				<div id="paragraph">
					(Updated 1/16/2007) <br>
					New features include: <br>
					<ul id="paragraph">
						<li> Publication quality image generation.
							<ul>
								<li> Node label position adjustment.
								
								<li> Automatic Visual Legend generator.
								
								<li> Node position fine-tuning by arrow keys.
								
								<li> The ability to override selected VizMap settings. 
							
							</ul>
						<li>Quick Find plugin.
						
						<li>New icons for a cleaner user interface.
						
						<li>Consolidated network import capabilities.
							<ul>
								<li> Import network from remote data sources (through http or ftp).
							
								<li> Default support for the following file formats: SBML, BioPAX, PSI-MI, Delimited text, Excel.
						
							</ul>
						<li>New Ontology Server.
							<ul>
								<li> Native support for OBO format ontology files. 
								
								<li> Ability to visualize the ontology tree as a network (DAG).
								
								<li> Full support for Gene Association files. 
							
							</ul>
						<li>Support for Java SE 5
						
						<li> Many, many bug fixes! 
					
					</ul>
					
      	See the <a href="cyto_2_4_features.php">Release Notes</a> for more detail. 
      		</div>
		</div>
		
		<div class="item">
			<h2>Publications about Cytoscape</h2>
			
			<div id="paragraph">
			<div class="pub">
				Melissa S Cline, Michael Smoot, Ethan Cerami, Allan Kuchinsky, Nerius Landys, 
				Chris Workman, Rowan Christmas, Iliana Avila-Campilo, Michael Creech, 
				Benjamin Gross, Kristina Hanspers, Ruth Isserlin, Ryan Kelley, Sarah Killcoyne, 
				Samad Lotia, Steven Maere, John Morris, Keiichiro Ono, Vuk Pavlovic, 
				Alexander R Pico, Aditya Vailaya, Peng-Liang Wang, Annette Adler, Bruce R Conklin, 
				Leroy Hood, Martin Kuiper, Chris Sander, Ilya Schmulevich, Benno Schwikowski, 
				Guy J Warner, Trey Ideker & Gary D Bader
				<h3>Integration of biological networks and gene expression data using Cytoscape</h3>
				Nature Protocols 2, 2366 - 2382 (2007) Published online: 27 September 2007 
				| doi:10.1038/nprot.2007.324<br><br>
					<a href="http://www.ncbi.nlm.nih.gov/pubmed/17947979"> [PubMed entry]</a>.
			</div>
			</div>
			
			<div id="paragraph">
			<div class="pub">
				Shannon P, Markiel A, Ozier O, Baliga NS, Wang JT, Ramage D, Amin N, 
				Schwikowski B, Ideker T.<br>
				<h3>Cytoscape: a software environment for integrated models of 
				biomolecular interaction networks.</h3>
				Genome Research 2003 Nov; 13(11):2498-504<br><br>
					<a href="http://www.genome.org/cgi/content/full/13/11/2498"> [Abstract] </a>
					<a href="http://www.genome.org/cgi/reprint/13/11/2498"> [PDF] </a>
					<a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&amp;db=PubMed&amp;list_uids=14597658&amp;dopt=Abstract"> [PubMed entry]</a>.
			</div>
			</div>
		</div>
			<div class="item">
				<h2><i>Updated!&nbsp;</i>(July 24 2007)</i> Research using Cytoscape</h2>
				
				<div id="paragraph">
					<strong>As of July 2007, 281 publications are citing 
					<a href="http://www.genome.org/cgi/content/full/13/11/2498">Shannon et al. (2003)</a>.</strong><br>
					<ul id="paragraph">
						<li><a href="http://scholar.google.com/scholar?hl=en&lr=&cites=3669641697993554798">View Full Listing at Google Scholar</a></li>
						<li><a href="http://www.pubmedcentral.nih.gov/tocrender.fcgi?action=cited&artid=403769">Link to Listing at PubMed Central</a></li>
						<li><a href="http://highwire.stanford.edu/cgi/searchresults?fulltext=cytoscape&andorexactfulltext=and&author1=&pubdate_year=&volume=&firstpage=&src=hw&searchsubmit=redo&resourcetype=1&search=Search&fmonth=Jan&fyear=1844&tmonth=Jul&tyear=2007&fdatedef=1+January+1844&tdatedef=24+Jul+2007">
								Link to HighWire Press</a>
					</ul>
									
					<p>[<a href="pubs.php">Link to Publications Page</a>]</p>
					
					<p><br>
						<b> Note: </b> If you have a publication which makes use of Cytoscape, please let us know by sending an email to the <a href="http://groups-beta.google.com/group/cytoscape-discuss"> cytoscape-discuss </a> mailing list. 
    </p>
				</div>
			</div>
			<div class="item">
				<div id="paragraph">
					 Past news articles are available <a href="past_news.php">here.</a></div>
			</div>
		</div>
		
		
		<div id="rightbox">
			<? include "help.php"; ?>
			<?
			if ($news_option == "atom") {
				include "feed.php";
			}else {
				include "news.php";
			}
			?>

			<? include "community_box.php"; include "collab.php"; ?>

		</div>
		<p><? include "footer.php"; ?></p>
	</div>
	</body>

</html>
