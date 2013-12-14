package name.richardson.james.bukkit.hearthstone;

import java.io.IOException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;

import name.richardson.james.bukkit.utilities.listener.AbstractListener;

public class MetricsListener extends AbstractListener {

	private final Metrics metrics;

	public MetricsListener(final JavaPlugin plugin, PluginManager pluginManager, final int homeCount) throws IOException {
		super(plugin, pluginManager);
		this.metrics = new Metrics(plugin);
		this.metrics.start();
		final Metrics.Graph graph = this.metrics.createGraph("Usage Statistics");
		graph.addPlotter(new Metrics.Plotter("Total homes") {
			@Override
			public int getValue() {
				return homeCount;
			}
		});
	}

}
