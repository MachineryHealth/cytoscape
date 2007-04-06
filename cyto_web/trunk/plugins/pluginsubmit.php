<?php

// mode = 'new', Data is submited by user
// mode = 'edit', Cytostaff edit the data in CyPluginDB
$mode = 'new'; // 'new' or 'edit', by default it is 'new'

// For edit mode only
$versionID = NULL; // used for edit mode only

if (isset ($_GET['versionid'])) {
	$versionID = $_GET['versionid'];
}
if (isset ($_POST['versionID'])) { // hidden field
	$versionID = $_POST['versionID'];
}

if ($versionID != NULL) {
	$mode = 'edit';
}

if ($mode == 'new') {
	$pageTitle = 'Submit plugin to Cytoscape';
} else
	if ($mode == 'edit') {
		$pageTitle = 'Edit plugin in CyPluginDB';
	} else {
		exit('Unknown page mode, mode must be either new or edit');
	}

?>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
	<title><?php echo $pageTitle;?></title>
	<link rel="stylesheet" type="text/css" media="screen" href="/css/cytoscape.css">
	<link rel="shortcut icon" href="images/cyto.ico">
	<style type="text/css">
<!--
.style3 {color: #FF0066}
.style4 {color: #FF0000}
-->
    </style>
</head>
<body bgcolor="#ffffff">
<table id="feature" border="0" cellpadding="0" cellspacing="0" summary="">
	<tbody>
		<tr>
			<td width="10">&nbsp;
			</td>
			<td valign="bottom">
				<h1><?php echo $pageTitle; ?></h1>
			</td>
		</tr>
	</tbody>
</table>

<?php include "../nav.php"; ?>
  
<?php

$tried = NULL;
if (isset ($_POST['tried'])) {
	$tried = 'yes';
}

// Include the DBMS credentials
include 'db.inc';

// Connect to the MySQL DBMS
if ($mode == 'edit') {
	if (!($connection = @ mysql_pconnect($dbServer, $cytostaff, $cytostaffPass)))
		showerror();
}
else // $mode == 'new'
{
	if (!($connection = @ mysql_pconnect($dbServer, $dbUser, $dbPass)))
		showerror();
}
// Use the CyPluginDB database
if (!mysql_select_db($dbName, $connection))
	showerror();

// initialize the variables
$name = NULL; // plugin name
$version = NULL;
$description = NULL;
$projectURL = NULL;
$category = NULL;
$releaseDate = NULL;
$month = NULL;
$day = NULL;
$year = NULL;
$releaseNote = NULL;
$releaseNoteURL = NULL;
$fileUpload = NULL;
$jarURL = NULL;
$sourceURL = NULL;
$Cy2p0_checked = NULL;
$Cy2p1_checked = NULL;
$Cy2p2_checked = NULL;
$Cy2p3_checked = NULL;
$Cy2p4_checked = NULL;
$Cy2p5_checked = NULL;
$cyVersion = NULL;
$reference = NULL;
$comment = NULL;
$names = NULL; // author names
$emails = NULL;
$affiliations = NULL;
$affiliationURLs = NULL;

// Case for 'edit', pull data out of DB for the given versionID
if (($tried == NULL) && ($mode == 'edit')) {

	// Pull the data out of DB for this versionID
	$query = 'SELECT categories.name as catName,' .
	' categories.category_id as catID, ' .
	' plugin_list.name as pluginName, ' .
	' plugin_list.description as pluginDescription, ' .
	' plugin_list.license_brief as pluginLicenseBrief, ' .
	' plugin_list.license_detail as pluginLicenseDetail, ' .
	' plugin_list.project_url as projectURL, ' .
	' plugin_version.plugin_file_id as pluginFileID, ' .
	' plugin_version.version as pluginVersion, ' .
	' plugin_version.release_date as releaseDate, ' .
	' plugin_version.release_note as releaseNote, ' .
	' plugin_version.release_note_url as releaseNoteURL, ' .
	' plugin_version.comment as comment, ' .
	' plugin_version.jar_url as jarURL, ' .
	' plugin_version.source_url as sourceURL, ' .
	' plugin_version.cy_version as cyVersion, ' .
	' plugin_version.reference as reference' .
	' FROM categories, plugin_list, plugin_version' .
	' WHERE  categories.category_id     = plugin_list.category_id and ' .
	'    plugin_list.plugin_auto_id = plugin_version.plugin_id and ' .
	'    plugin_version.version_auto_id = ' . $versionID;

	// Run the query
	if (!($result = @ mysql_query($query, $connection)))
		showerror();

	$first_row = @ mysql_fetch_array($result);
	$category = $first_row['catName'];
	$categoryID = $first_row['catID'];
	$name = $first_row['pluginName'];
	$description = $first_row['pluginDescription'];
	$version = $first_row['pluginVersion'];
	$releaseDate = $first_row['releaseDate'];
	list ($year, $month, $day) = split('[-]', $releaseDate);
	$projectURL = $first_row['projectURL'];
	$releaseNote = $first_row['releaseNote'];
	$releaseNoteURL = $first_row['releaseNoteURL'];

	//fileUpload

	$jarURL = $first_row['jarURL'];
	$sourceURL = $first_row['sourceURL'];

	$cyVersion = $first_row['cyVersion'];
	$theVersions = preg_split("{,}", $cyVersion); // Split into an array

	foreach ($theVersions as $theVersion) {
		if ($theVersion == '2.0') {
			$Cy2p0_checked = "checked";
		}
		if ($theVersion == '2.1') {
			$Cy2p1_checked = "checked";
		}
		if ($theVersion == '2.2') {
			$Cy2p2_checked = "checked";
		}
		if ($theVersion == '2.3') {
			$Cy2p3_checked = "checked";
		}
		if ($theVersion == '2.4') {
			$Cy2p4_checked = "checked";
		}
		if ($theVersion == '2.5') {
			$Cy2p5_checked = "checked";
		}
	}

	$reference = $first_row['reference'];

	// get the author info for this versionID
	$query = 'select * from authors, plugin_author ' .
	' where authors.author_auto_id = plugin_author.author_id and plugin_version_id =' . $versionID .
	' order by plugin_author.authorship_seq';
	// Run the query
	if (!($result = @ mysql_query($query, $connection)))
		showerror();
	//$authorCount = mysql_num_rows($result);	
	while ($author_row = @ mysql_fetch_array($result)) {
		$names[] = $author_row['names'];
		$emails[] = $author_row['email'];
		$affiliations[] = $author_row['affiliation'];
		$affiliationURLs[] = $author_row['affiliationURL'];
	}
}

// if form validation failed
if (isset ($_POST['tfName'])) {
	$name = $_POST['tfName'];
}

if (isset ($_POST['tfVersion'])) {
	$version = $_POST['tfVersion'];
}

if (isset ($_POST['taDescription'])) {
	$description = $_POST['taDescription'];
}

if (isset ($_POST['tfProjectURL'])) {
	$projectURL = $_POST['tfProjectURL'];
}

if (isset ($_POST['optCategory'])) {
	$category = $_POST['optCategory'];
}

if (isset ($_POST['tfMonth'])) {
	$month = $_POST['tfMonth'];
}

if (isset ($_POST['tfDay'])) {
	$day = $_POST['tfDay'];
}

if (isset ($_POST['tfYear'])) {
	$year = $_POST['tfYear'];
	$releaseDate = $year . '-' . $month . '-' . $day;
}

if (isset ($_POST['taReleaseNote'])) {
	$releaseNote = $_POST['taReleaseNote'];
}

if (isset ($_POST['tfReleaseNoteURL'])) {
	$releaseNoteURL = $_POST['tfReleaseNoteURL'];
}

if (isset ($_FILES['filePlugin'])) {
	$fileUpload = $_FILES['filePlugin'];
}

if (isset ($_POST['tfJarURL'])) {
	$jarURL = $_POST['tfJarURL'];
}

if (isset ($_POST['tfSourceURL'])) {
	$sourceURL = $_POST['tfSourceURL'];
}

if (isset ($_POST['chk2p0'])) {
	$Cy2p0_checked = "checked";
	$cyVersion = '2.0';
}
if (isset ($_POST['chk2p1'])) {
	$Cy2p1_checked = "checked";
	if ($cyVersion == NULL) {
		$cyVersion = '2.1';
	} else {
		$cyVersion .= ',2.1';
	}
}
if (isset ($_POST['chk2p2'])) {
	$Cy2p2_checked = "checked";
	if ($cyVersion == NULL) {
		$cyVersion = '2.2';
	} else {
		$cyVersion .= ',2.2';
	}
}
if (isset ($_POST['chk2p3'])) {
	$Cy2p3_checked = "checked";
	if ($cyVersion == NULL) {
		$cyVersion = '2.3';
	} else {
		$cyVersion .= ',2.3';
	}
}
if (isset ($_POST['chk2p4'])) {
	$Cy2p4_checked = "checked";
	if ($cyVersion == NULL) {
		$cyVersion = '2.4';
	} else {
		$cyVersion .= ',2.4';
	}
}
if (isset ($_POST['chk2p5'])) {
	$Cy2p5_checked = "checked";
	if ($cyVersion == NULL) {
		$cyVersion = '2.5';
	} else {
		$cyVersion .= ',2.5';
	}
}
if (isset ($_POST['taReference'])) {
	$reference = $_POST['taReference'];
}

if (isset ($_POST['taComment'])) {
	$comment = $_POST['taComment'];
}

//Authors
if (isset ($_POST['tfNames0'])) {
	$names[0] = $_POST['tfNames0'];
}
if (isset ($_POST['tfEmail0'])) {
	$emails[0] = $_POST['tfEmail0'];
}
if (isset ($_POST['tfAffiliation0'])) {
	$affiliations[0] = $_POST['tfAffiliation0'];
}
if (isset ($_POST['tfAffiliationURL0'])) {
	$affiliationURLs[0] = $_POST['tfAffiliationURL0'];
}

if (isset ($_POST['tfNames1'])) {
	$names[1] = $_POST['tfNames1'];
}
if (isset ($_POST['tfEmail1'])) {
	$emails[1] = $_POST['tfEmail1'];
}
if (isset ($_POST['tfAffiliation1'])) {
	$affiliations[1] = $_POST['tfAffiliation1'];
}
if (isset ($_POST['tfAffiliationURL1'])) {
	$affiliationURLs[1] = $_POST['tfAffiliationURL1'];
}

// Detect the action button clicked
$submitAction = NULL;
if (isset ($_POST['btnSubmit'])) {
	$submitAction = $_POST['btnSubmit'];
}

//////////////////////// Form validation ////////////////////////
$validated = true;

if ($tried != NULL && $tried == 'yes') {

	if (empty ($_POST['tfName'])) {
		$validated = false;
?>
		Error: Plugin_name is a required field.<br>
		<?php


	}
	if (empty ($_POST['tfVersion'])) {
		$validated = false;
?>
		Error: Version is a required field.<br>
		<?php


	}
	if (empty ($_POST['taDescription'])) {
		$validated = false;
?>
		Error: Description is a required field.<br>
		<?php


	}
	if ($category == "Please choose one") {
		$validated = false;
?>
		Error: Category is a required field.<br>
		<?php


	}

	// validate the release date
	if (!(empty ($month) && empty ($day) && empty ($year))) {
		if (!((strspn($month, "0123456789") == strlen($month)) && (strlen($month) > 0) && (strlen($month) < 3))) {
			$validated = false;
?>
			Invalid release month <br>
			<?php


		}
		if (!((strspn($day, "0123456789") == strlen($day)) && (strlen($day) > 0) && (strlen($day) < 3))) {
			$validated = false;
?>
			Invalid release day <br>
			<?php


		}
		if (!((strspn($year, "0123456789") == strlen($year)) && (strlen($year) > 0) && (strlen($year) == 4))) {
			$validated = false;
?>
			Invalid release year <br>
			<?php


		}
	}

	//Either a jarURL or a jar file should supplied
	if (empty ($_POST['tfJarURL']) && empty ($_FILES['filePlugin']['name'])) {
		$validated = false;
?>
		Error: Either a jarURL or a jar file should be supplied.<br>
		<?php


	}

} // End of form validation

// Check if the plugin already existed, if the mode is 'new' (i.e. bubmit from user)
if ($tried != NULL && $tried == 'yes' && $validated && $mode == 'new') {
	$query = 'SELECT version_auto_id FROM categories, plugin_list, plugin_version' .
	' WHERE categories.category_id = plugin_list.category_id ' .
	'       and plugin_list.plugin_auto_id = plugin_version.plugin_id ' .
	'       and categories.name ="' . $category . "\" " .
	'		and plugin_list.name = "' . $name . "\" " .
	'		and plugin_version.version = "' . $version . "\"";

	// Run the query
	if (!($result = @ mysql_query($query, $connection)))
		showerror();

	if (@ mysql_num_rows($result) != 0) {
		$validated = false;
?>
			Error: The version of this plugin already existed.<br>
			<?php


	}
}

//echo "tried = ", $tried, "  validated = ",$validated,"<br>"; 

/////////////////////////////////  Form definition //////////////////////////

if (!($tried && $validated)) {
?>
</p>
<blockquote>
  <p><SPAN id="_ctl3_LabelRequired">	Fields denoted   by an (<span class="style4">*</span>) are required.</SPAN></p>
</blockquote>
<form action="<?php echo $_SERVER['PHP_SELF'] ?>" method="post" enctype="multipart/form-data" name="submitplugin" id="submitplugin">
  <table width="878" border="0">
  <tr>
    <td width="208"><div align="right" ><span class="style3">*</span>Plugin name</div></td>
    <td width="660"><input name="tfName" type="text" value ="<?php echo $name ?>" size="40" /></td>
  </tr>
  <tr>
    <td><div align="right"><span class="style4">*</span>version</div>      </td>
    <td><input name="tfVersion" type="text" id="tfVersion" value ="<?php echo $version ?>" size="20" /></td>
  </tr>
  <tr>
    <td height="75"><div align="right"><span class="style4">*</span>Description</div></td>
    <td><textarea name="taDescription" cols="80" rows="5" id="taDescription"><?php echo $description ?></textarea></td>
  </tr>
    <tr>
    <td><div align="right"><span class="style4">*</span>Category</div></td>
    <td><label>
      <select name="optCategory" id="optCategory" >
        <option "<?php if ($category && $category == 'Please choose one') echo 'selected' ?>">Please choose one</option>
        <option "<?php if ($category && $category == 'Analysis Plugins') echo 'selected' ?>">Analysis Plugins</option>
        <option "<?php if ($category && $category == 'Network and Attribute I/O Plugins') echo 'selected' ?>">Network and Attribute I/O Plugins</option>
        <option "<?php if ($category && $category == 'Network Inference Plugins') echo 'selected' ?>">Network Inference Plugins</option>
        <option "<?php if ($category && $category == 'Functional Enrichment Plugins') echo 'selected' ?>">Functional Enrichment Plugins</option>
        <option "<?php if ($category && $category == 'Communication/Scripting Plugins') echo 'selected' ?>">Communication/Scripting Plugins</option>
      </select>
    </label></td>
  </tr>
  <tr>
    <td><div align="right"><span class="style4">*</span>Release Date </div></td>
    <td><table width="118" border="0">
        <tr>
          <td width="32" scope="col"><div align="center">mm</div></td>
          <td width="29" scope="col"><div align="center">dd</div></td>
          <td width="57" scope="col"><div align="center">yyyy</div></td>
        </tr>
        <tr>
          <td><input name="tfMonth" type="text" id="tfMonth" value ="<?php echo $month ?>" size="2" /></td>
          <td><input name="tfDay" type="text" id="tfDay" value ="<?php echo $day ?>" size="2" /></td>
          <td><input name="tfYear" type="text" id="tfYear" value ="<?php echo $year ?>" size="4" /></td>
        </tr>
      </table>      </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><label></label></td>
  </tr>
  <tr>
    <td><div align="right">Jar File </div></td>
    <td><input name="filePlugin" type="file" id="filePlugin" size="80" /></td>
  </tr>
  <tr>
    <td><div align="right"><span class="style4">*</span>or</div></td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td><div align="right">Jar URL </div></td>
    <td><input name="tfJarURL" type="text" id="jarURL" value ="<?php echo $jarURL ?>" size="80" /></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><label></label></td>
  </tr>
  <tr>
    <td><div align="right"><span class="style4">*</span>Cytoscape versions </div></td>
    <td><table width="404" border="0">
      <tr>
        <td width="72"><label>2.0
            <input name="chk2p0" type="checkbox" id="chk2p0" value="Cy2p0" <?php echo $Cy2p0_checked ?> />
        </label></td>
        <td width="73"><label>2.1
            <input name="chk2p1" type="checkbox" id="chk2p1" value="Cy2p1" <?php echo $Cy2p1_checked ?> />
        </label></td>
        <td width="72"><label>2.2
            <input name="chk2p2" type="checkbox" id="chk2p2" value="Cy2p2" <?php echo $Cy2p2_checked ?> />
        </label></td>
        <td width="72"><label>2.3
            <input name="chk2p3" type="checkbox" id="chk2p3" value="Cy2p3" <?php echo $Cy2p3_checked ?> />
        </label></td>
        <td width="72"><label>
          2.4
          <input name="chk2p4" type="checkbox" id="chk2p4" value="Cy2p4" <?php echo $Cy2p4_checked ?> />
        </label></td>
        <td width="135"><label>
          2.5
          <input name="chk2p5" type="checkbox" id="chk2p5" value="Cy2p5" <?php echo $Cy2p5_checked ?> />
        </label></td>
      </tr>
    </table></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><label></label></td>
  </tr>
  <tr>
    <td><div align="right">Author(s)</div></td>
    <td><table width="660" border="0">
      <tr>
        <td width="444"><div align="center"> Name(s)</div></td>
        <td width="206"><div align="center">contact e-mail (not made public) </div></td>
      </tr>
      <tr>
        <td><label>
          <input name="tfNames0" type="text" id="tfNames0" size="70" value ="<?php echo $names[0] ?>" />
        </label></td>
        <td><input name="tfEmail0" type="text" id="tfEmail0" size="30" value ="<?php echo $emails[0] ?>" /></td>
        </tr>
      <tr>
        <td><div align="center">Affiliation</div></td>
        <td><div align="center">Affiliation URL</div></td>
        </tr>
      <tr>
        <td><input name="tfAffiliation0" type="text" id="tfAffiliation0" size="70" value ="<?php echo $affiliations[0] ?>" /></td>
        <td><input name="tfAffiliationURL0" type="text" id="tfAffiliationURL0" size="30" value ="<?php echo $affiliationURLs[0] ?>" /></td>
      </tr>
    </table></td>
  </tr>

  <tr>
    <td>&nbsp;</td>
    <td><table width="660" border="0">
      <tr>
        <td width="444"><div align="center"> Name(s)</div></td>
        <td width="206"><div align="left">contact e-mail </div></td>
      </tr>
      <tr>
        <td><label>
        <input name="tfNames1" type="text" id="tfNames1" size="70" value ="<?php echo $names[1] ?>" />
        </label></td>
        <td><input name="tfEmail1" type="text" id="tfEmail02" size="30" value ="<?php echo $emails[1] ?>" /></td>
      </tr>
      <tr>
        <td><div align="center">Affiliation</div></td>
        <td><div align="center">Affiliation URL</div></td>
      </tr>
      <tr>
        <td><input name="tfAffiliation1" type="text" id="tfAffiliation1" size="70" value ="<?php echo $affiliations[1] ?>" /></td>
        <td><input name="tfAffiliationURL1" type="text" id="tfAffiliationURL1" size="30" value ="<?php echo $affiliationURLs[1] ?>" /></td>
      </tr>
    </table></td>
  </tr>
  
  <tr>  
<td>&nbsp;</td>
<td>&nbsp;</td>
  </tr>

  
  <tr>
    <td><div align="right">Project URL</div></td>
    <td><input name="tfProjectURL" type="text" id="tfProjectURL" value ="<?php echo $projectURL ?>" size="80" /></td>
  </tr>
  <tr>
    <td><div align="right">Release note</div></td>
    <td><label>
      <textarea name="taReleaseNote" cols="80" rows="3" id="taReleaseNote"></textarea>
    </label></td>
  </tr>
  <tr>
    <td><div align="right">Release note URL</div></td>
    <td><input name="tfReleaseNoteURL" type="text" id="tfReleaseNoteURL" value ="<?php echo $releaseNoteURL ?>" size="80"></td>
  </tr>
  <tr>
    <td><div align="right">Source URL</div></td>
    <td><input name="tfSourceURL" type="text" id="tfSourceURL" value ="<?php echo $sourceURL ?>" size="80"></td>
  </tr>
  <tr>
    <td><div align="right">Reference</div></td>
    <td><textarea name="taReference" cols="80" rows="3" id="taReference"></textarea></td>
  </tr>
  <tr>
    <td><div align="right">License (brief) </div></td>
    <td><label>
    <input name="tfLicenseBrief" type="text" id="tfLicenseBrief" size="80">
    </label></td>
  </tr>
  <tr>
    <td><div align="right">License (detail)</div></td>
    <td><label>
      <textarea name="taLicenseDetail" cols="80" rows="3" id="taLicenseDetail"></textarea>
    </label></td>
  </tr>
  <tr>
    <td><div align="right">Comment</div></td>
    <td><label>
      <textarea name="taComment" cols="80" rows="2" id="taComment"></textarea>
    </label></td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td><input name="tried" type="hidden" id="tried" value="yes">
      <input name="versionID" type="hidden" id="versionID" value="<?php echo $versionID; ?>"></td>
  </tr>
</table>
<p align="center">
<?php

	if ($mode == 'new') {
?>
<input name="btnSubmit" type="submit" id="btnSubmit" value="Submit" />
<?php

	} else
		if ($mode == 'edit') {
?>	
	<p align="center">
	  <input name="btnSubmit" type="submit" id="btnSubmit" value="Save" />
	  &nbsp;&nbsp;
	  <input name="btnSubmit" type="submit" id="btnSubmit" value="Save and publish" />
	  &nbsp;&nbsp;
	  <input name="btnSubmit" type="submit" id="btnSubmit" value="Save and unpublish" />
  </p>
</form>
	<p align="center">&nbsp;</p>
	<?php

		}
?>
</p>
</form>

<?php


} else
	////////////////////////// form processing /////////////////////////
	// if mode = 'new', takes the details of the plugin from user and adds them to the tables of our CyPluginDB, with status = 'new'.
	// if mode = 'Edit', update the plugin info in CyPluginDB, change status based on button pressed.

	{

	//echo 'submitAction = ', $submitAction, '<br>';
	// In case of edit, do updating
	if ($mode == 'edit') {
		$status = NULL;
		if ($submitAction == 'Save') { // Edit, for save only, do not change status
			$status = 'Do not change';
		}
		else if ($submitAction == 'Save and publish') { // Edit, for save only, do not change status
			$status = 'published';
		}
		if ($submitAction == 'Save and unpublish') { // Edit, for save only, do not change status
			$status = 'new';
		}
		if ($status != 'Do not change') {
			$query = 'update plugin_version set status = "'.$status.'" where version_auto_id = '.$versionID;
			//echo 'query =',$query,'<br>';
			if (!(@ mysql_query($query, $connection)))
				showerror();		
		}
		
		echo '<br>The plugin status has been updated.<br>';



	} // case for mode = 'edit'

	//exit ("Exit before data processing <br>");

	if ($mode == 'new') {
		//$submitAction == 'Submit', accept data submited from user
		//process the data and Save the data into DB.

		//Load the Jar file to DB if any
		$plugin_file_auto_id = NULL;

		if ($fileUpload['name'] != NULL) {
			//echo "A file is selected";
			$fileUpload_type = $fileUpload['type'];
			$fileUpload_name = $fileUpload['name'];

			$fileHandle = fopen($fileUpload['tmp_name'], "r");
			$fileContent = fread($fileHandle, $fileUpload['size']);
			$fileContent = addslashes($fileContent);

			$dbQuery = "INSERT INTO plugin_files VALUES ";
			$dbQuery .= "(0, '$fileContent', '$fileUpload_type', '$fileUpload_name')";
			//echo "<br>dbQuery = " . $dbQuery . "<br>";
			// Run the query
			if (!(@ mysql_query($dbQuery, $connection)))
				showerror();

			echo "<br><b>File uploaded successfully</b><br>";
			$plugin_file_auto_id = mysql_insert_id($connection);
		}

		// Get the category_id
		$dbQuery = 'SELECT category_id FROM categories WHERE name = "' . $category . '"';
		// Run the query
		if (!($result = @ mysql_query($dbQuery, $connection)))
			showerror();

		$the_row = @ mysql_fetch_array($result);
		$category_id = $the_row['category_id'];

		$plugin_auto_id = NULL;
		//Check if there is an old version of this plugin in DB
		$dbQuery = 'SELECT plugin_auto_id FROM plugin_list ' .
		'         WHERE plugin_list.name = "' . $name . '" and category_id =' . $category_id;

		// Run the query
		if (!($result = @ mysql_query($dbQuery, $connection)))
			showerror();

		if (@ mysql_num_rows($result) != 0) {
			//There is an old version in the DB, update the row in the table plugin_list
			$the_row = @ mysql_fetch_array($result);
			$plugin_auto_id = $the_row['plugin_auto_id'];
			echo "There is an old version of this plugin in the DB, plugin_auto_id =" . $plugin_auto_id . "<br>";
		} else {
			//This is a new plugin, add a row in the table plugin_list
			//echo "This is a new plugin<br>";

			$dbQuery = 'INSERT INTO plugin_list VALUES ' .
			'(0, "' . $name . '", "' . $description . '",NULL,NULL,"' . $projectURL . '",' .
			$category_id . ',now())';
			//echo "<br>dbQuery = " . $dbQuery . "<br>";
			// Run the query
			if (!($result = @ mysql_query($dbQuery, $connection)))
				showerror();

			$plugin_auto_id = mysql_insert_id($connection);
			//echo "new plugin_auto_id = " . $plugin_auto_id . "<br>";
		}

		// Insert a row into table plugin_version
		$status = 'new';
		$dbQuery = 'INSERT INTO plugin_version VALUES (0, ' . $plugin_auto_id . ', ';
		if ($plugin_file_auto_id == NULL) {
			$dbQuery .= 'NULL';
		} else {
			$dbQuery .= $plugin_file_auto_id;
		}
		$dbQuery .= ',"' . $version . '",\'' .
		$releaseDate . '\',"' . $releaseNote . '","' . $releaseNoteURL . '","' . $comment . '","' . $jarURL . '","' .
		$sourceURL . '","' . $cyVersion . '","' . $status . '","' . $reference . '", now())';

		//echo "<br>dbQuery = " . $dbQuery . "<br>";

		// Run the query
		if (!(@ mysql_query($dbQuery, $connection)))
			showerror();

		$version_auto_id = mysql_insert_id($connection);
		//echo "new version_auto_id = " . $version_auto_id . "<br>";

		// insert rows into author tables (authors and plugin_author)

		$authorCount = count($names);

		for ($i = 0; $i < $authorCount; $i++) {
			$dbQuery = 'INSERT INTO authors VALUES (0, "' . $names[$i] . '", "' . $emails[$i] . '","' . $affiliations[$i] . '","' . $affiliationURLs[$i] . '")';

			//echo "<br>dbQuery = " . $dbQuery . "<br>";

			// Run the query
			if (!(@ mysql_query($dbQuery, $connection)))
				showerror();

			$author_auto_id = mysql_insert_id($connection);
			//echo "new author_auto_id = " . $author_auto_id . "<br>";

			$authorship_seq = $i;
			$dbQuery = 'INSERT INTO plugin_author VALUES (' . $version_auto_id . ', ' . $author_auto_id . ',' . $authorship_seq . ')';

			//echo "<br>dbQuery = " . $dbQuery . "<br>";

			// Run the query
			if (!(@ mysql_query($dbQuery, $connection)))
				showerror();
		}
?>
	Thank you for submitting your plugin to Cytoscape.Cytoscape staff will review the data  and publish it on the cytoscape website. If there are any questions, you will be contacted via e-mail.
	<?php
	} // end of form processing
}// case for mode == 'new'
?>

<?php include "../footer.php"; ?>
<br>
</body>
</html>
