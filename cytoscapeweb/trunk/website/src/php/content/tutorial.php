<div class="left">
    
    <h1>Introduction</h1>
    
    <p>This tutorial guides you through the process of getting Cytoscape Web up and running within
    a HTML page.  Once you have Cytoscape Web working, you can continue on in the tutorial to see
    how to interact with Cytoscape Web.  Additionally, visual styles are presented as a more
    complex example of how to interact with Cytoscape Web.</p>




    <h1>Getting started</h1>
    
    
    <h2>What you need</h2>
    
    <p>All the files you need are in the latest version of the Cytoscape Web distribution archive.
    <a href="/download#now">Get the latest version of the archive</a>, and extract the files.</p>
    
    <p>When opening the examples as a local files in your browser, you may not be able to see
    Cytoscape Web.  This is due to Flash security settings.  Make sure to allow Flash to run
    from the local filesystem (<code>file://*</code>) in the 
    <a href="http://www.macromedia.com/support/documentation/en/flashplayer/help/settings_manager04.html">security settings panel</a>.
      You do not need to change the security settings if you deploy Cytoscape Web on a web server, 
      such as Apache.</p>
    
    <img src="/img/content/tutorial/flash_security.png" />
    
    <h2>What to do</h2>
    
    <p>The best way to familiarise yourself with setting up Cytoscape Web is to go through an
    example.</p>
    
    <p>It is important to note that Cytoscape Web does not load remote files for you.  So if you
    have a graph file you want to load from a server, you must load the file into a
    <code>string</code>, either by hardcoding the graph into your code or loading the graph file via
    AJAX first.  We recommend you take a look at <a href="http://jquery.com" rel="external">jQuery</a>.
    It makes Javascript <em>really</em> easy, especially
    <a href="http://docs.jquery.com/Ajax" rel="external">AJAX</a>.</p>
    
    <p>Now, take a look at this example.  It has everything needed to get Cytoscape Web up and
    running.</p>
    
    <?php print_code("file/example_code/getting_started.html"); ?>
    
    <p>The code above is embedded below.  If you are following along, you can copy and paste the
    code above and adjust the <code>script</code> tag references to the path where you extracted
    Cytoscape Web.  When loaded in your browser, the file you would have made would look just
    like the embedded code below.</p>
    
    <?php embed_code("file/example_code/getting_started.html"); ?>
    



    
    <h1>Interacting with Cytoscape Web</h1>
    
    <p>Now that you are able to embed Cytoscape Web into a page, you can use the Javascript API to
    interact with it.  You have already used the class representing the
    <a href="/documentation/visualization">embedded visualisation</a> in the
    first example.  What remains is to interact with the visualisation once it has been drawn.</p>
    
    <p>The main thing to keep in mind is that you can not interact with most of Cytoscape Web
    until the graph is drawn.  Thus, you can interact with Cytoscape Web by using the
    <a href="/documentation/visualization#ready">ready</a> callback function, which is called
    when Cytoscape Web is finished drawing the graph.</p>
    
    <p>This example interacts with Cytoscape Web by getting attributes values that were set for the
    nodes and edges in the graph.  This is achieved by registering with the
    <a href="/documentation/visualization#addListener">addListener</a> function for click events.</p>
    
    <?php print_code("file/example_code/interacting.html"); ?>
    
    <?php embed_code("file/example_code/interacting.html"); ?>
    
    <p>Now that you know how to initialise and interact with Cytoscape Web, you can look to the
    <a href="/documentation/visualization">API reference</a> to customise
    Cytoscape Web exactly to your liking.  Have fun!</p>
    
    <p>If you would like a bit more in the way of instruction, see the next section on how to
    set the visual style.  The example there is a bit more complex, but it should give you the
    opportunity to become more familiar with the Cytoscape Web API.</p>
    
    
    <h1>Visual styles</h1>
    
    <p>Visual styles configure the way that the graph is visually displayed.  You can create a
    visual style statically or programattically, by setting the visual style at initialisation or
    by using the <a href="/documentation#section/visualStyle">visualStyle</a> function.</p>
    
    <p>This example changes the visual style of the graph from the previous examples.</p>
    
    <?php print_code("file/example_code/style.html"); ?>
    
    <p>The style is set at initialisation.  Additionally, clicking the link changes the visual
    style programattically by changing the background color to a randomly selected color for
    each click.</p>
    
    <?php embed_code("file/example_code/style.html"); ?>
    
    <p>This example has used only a few visual properties for the sake of simplicity.  However,
    there are many visual properties that exist in Cytoscape Web that can be used to control
    exactly how things are visually displayed.  Take a look at the
    <a href="/documentation#section/visualStyle">visualStyle</a>
    function for more information.</p>
    
    
    
    
    <h1>Conclusion</h1>
    
    <p>This tutorial should allow you to have Cytoscape Web up and running and interacting with
    other components in your page.  You should now be sufficiently capable of using the
    <a href="/documentation/visualization">API reference</a> to customise Cytoscape Web to your
    specific needs.</p>
    
    <p>If you still have questions, take a look at the <a href="faq">FAQ</a>.  Most likely,
    your question has already been answered there.</p>
    
</div>