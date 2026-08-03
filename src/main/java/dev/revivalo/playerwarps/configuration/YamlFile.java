package dev.revivalo.playerwarps.configuration;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.revivalo.playerwarps.PlayerWarpsPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.logging.Level;

public class YamlFile {

	private final File file;
	private final UpdateMethod updateMethod;
	private final String filePath;
	private final YamlConfiguration configuration;

	public YamlFile(final String filePath, final File folder, final UpdateMethod updateMethod) {
		file = new File(folder, filePath);
		this.updateMethod = updateMethod;
		this.filePath = filePath;

		boolean update = true;
		switch (this.updateMethod){
			case NEVER:
				update = false;
				break;
			case ON_LOAD:
				update = !file.exists();
				break;
		}

		Validate.notNull(file, "File cannot be null");

		configuration = new YamlConfiguration();

		try {
			configuration.load(file);
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
			PlayerWarpsPlugin.get().getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
		} catch (InvalidConfigurationException ex) {
			PlayerWarpsPlugin.get().getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
			return;
		}

		save();

		configuration.options().copyDefaults(true);

		if (update) {
			try {
				if (PlayerWarpsPlugin.get().getResource(filePath) != null) {
					ConfigUpdater.update(PlayerWarpsPlugin.get(), filePath, file, Collections.emptyList());
				}
				reload();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public YamlConfiguration getConfiguration() {
		return configuration;
	}

	public void save() {
		try {
			this.configuration.save(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes already serialized content to the file. Kept separate from {@link #save()} so the
	 * write can be performed off the main thread without serializing there too. Synchronized to
	 * keep two overlapping writes from interleaving.
	 */
	public synchronized void write(String contents) {
		try {
			final File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}

			Files.writeString(file.toPath(), contents, StandardCharsets.UTF_8);
		} catch (IOException e) {
			PlayerWarpsPlugin.get().getLogger().log(Level.SEVERE, "Cannot save " + file, e);
		}
	}

	public void reload() {
		try {
			configuration.load(file);
		} catch (InvalidConfigurationException | IOException ex) {
			ex.printStackTrace();
		}
	}

	public File getFile() {
		return this.file;
	}

	public UpdateMethod getUpdateMethod() {
		return this.updateMethod;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public enum UpdateMethod {
		EVERYTIME, ON_LOAD, NEVER
	}
}
