package org.cytoscape.task.internal.proxysettings;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.Tunable.Param;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.work.TaskManager;
import org.cytoscape.work.ValuedTask;
import org.cytoscape.work.ValuedTaskExecutor;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

import java.net.URL;

import org.cytoscape.io.util.StreamUtil;

/**
 * Dialog for assigning proxy settings.
 * @author Pasteur
 */
public class ProxySettingsTask implements Task, TunableValidator
{
	static final List<String> KEYS = Arrays.asList("http.proxyHost", "http.proxyPort", "socks.proxyHost", "socks.proxyPort");

	@Tunable(description="Type")
	public ListSingleSelection<String> type = new ListSingleSelection<String>("direct", "http", "socks");

	@Tunable(description="Proxy Server",groups={"param"},dependsOn="type!=direct",alignment={Param.horizontal},groupTitles={Param.hidden})
	public String hostname="";

	@Tunable(description="Port",groups={"param"},dependsOn="type!=direct",alignment={Param.horizontal},groupTitles={Param.hidden})
	public int port=0;

	final TaskManager taskManager;
	final StreamUtil streamUtil;

	final Map<String,String> oldSettings = new HashMap<String,String>();
	final Properties properties = System.getProperties();

	public ProxySettingsTask(final TaskManager taskManager, final StreamUtil streamUtil)
	{
		this.taskManager = taskManager;
		this.streamUtil = streamUtil;
	}

	public void validate() throws Exception
	{
		storeProxySettings();

		ValuedTaskExecutor<Exception> executor = new ValuedTaskExecutor<Exception>(new TestProxySettings(streamUtil));
		taskManager.execute(executor);

		Exception result = null;
		try
		{
			result = executor.get();
		}
		catch (InterruptedException e) {}
		catch (ExecutionException e) {}
		catch (CancellationException e) {}

		revertProxySettings();

		if (result != null)
		{
			throw new Exception(String.format("Cytoscape was unable to connect to the internet because:\n\n%s", result.getMessage()));
		}
	}


	public void run(TaskMonitor taskMonitor)
	{
		storeProxySettings();
		oldSettings.clear();
	}

	public void cancel()
	{
	}

	void storeProxySettings()
	{
		oldSettings.clear();
		for (String key : KEYS)
			if (properties.getProperty(key) != null)
				oldSettings.put(key, properties.getProperty(key));

		if (type.getSelectedValue().equals("direct"))
		{
			for (String key : KEYS)
				if (properties.getProperty(key) != null)
					properties.remove(key);
		}
		else if (type.getSelectedValue().equals("http"))
		{
			properties.remove("socks.proxyHost");
			properties.remove("socks.proxyPort");
			properties.setProperty("http.proxyHost", hostname);
			properties.setProperty("http.proxyPort", Integer.toString(port));
		}
		else if (type.getSelectedValue().equals("socks"))
		{
			properties.remove("http.proxyHost");
			properties.remove("http.proxyPort");
			properties.setProperty("socks.proxyHost", hostname);
			properties.setProperty("socks.proxyPort", Integer.toString(port));
		}
	}

	void revertProxySettings()
	{
		for (String key : KEYS)
		{
			if (properties.getProperty(key) != null)
				properties.remove(key);
			
			if (oldSettings.containsKey(key))
				properties.setProperty(key, oldSettings.get(key));
		}
		oldSettings.clear();
	}

	void dumpSettings(String title)
	{
		System.out.println(title);
		for (String key : KEYS)
			System.out.println(String.format("%s: %s", key, properties.getProperty(key)));
	}
}

class TestProxySettings implements ValuedTask<Exception>
{
	static final String TEST_URL = "http://www.google.com";

	final StreamUtil streamUtil;

	public TestProxySettings(final StreamUtil streamUtil)
	{
		this.streamUtil = streamUtil;
	}

	public Exception run(TaskMonitor taskMonitor)
	{
		taskMonitor.setTitle("Testing Proxy Settings");
		try
		{
			taskMonitor.setStatusMessage("Attempting to open a URL...");
			URL url = new URL(TEST_URL);
			streamUtil.getInputStream(url).close();
		}
		catch (Exception ex)
		{
			return ex;
		}

		return null;
	}

	public void cancel()
	{
	}
}
