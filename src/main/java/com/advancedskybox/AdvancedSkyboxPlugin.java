package com.advancedskybox;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.JFileChooser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
		name = "Advanced Skybox"
)
public class AdvancedSkyboxPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "advancedskybox";

	@Inject
	private AdvancedSkyboxConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AdvancedSkyboxOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	private final PlaybackClock playbackClock = new PlaybackClock();

	private AdvancedSkyboxPanel panel;
	private NavigationButton navButton;

	@Getter
	private String packStatus = "No frame pack folder selected.";

	@Getter
	private boolean packValid = false;

	@Override
	protected void startUp()
	{
		overlay.setPlaybackClockMsSupplier(playbackClock::getElapsedMilliseconds);
		overlay.setPackStatusSupplier(this::getPackStatus);

		panel = new AdvancedSkyboxPanel(this);

		navButton = NavigationButton.builder()
				.tooltip("Advanced Skybox Controls")
				.icon(createSidebarIcon())
				.priority(Integer.MIN_VALUE)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(overlay);

		loadPackPaused();

		log.debug("Advanced Skybox started!");
	}

	@Override
	protected void shutDown()
	{
		playbackClock.pause();
		overlay.pausePlayback();

		overlayManager.remove(overlay);

		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}

		panel = null;

		log.debug("Advanced Skybox stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		if ("packFolderPath".equals(event.getKey()))
		{
			loadPackPaused();
		}

		updatePanelStatus();
	}

	public void choosePackFolder()
	{
		JFileChooser chooser = new JFileChooser(config.packFolderPath());
		chooser.setDialogTitle("Select Frame Pack Folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		int result = chooser.showOpenDialog(null);

		if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null)
		{
			return;
		}

		String selectedPath = chooser.getSelectedFile().getAbsolutePath();

		configManager.setConfiguration(CONFIG_GROUP, "packFolderPath", selectedPath);

		loadPackPausedFromPath(selectedPath);
		updatePanelStatus();
	}

	public void pauseMedia()
	{
		playbackClock.pause();
		overlay.pausePlayback();

		packStatus = "Paused.";
		updatePanelStatus();
	}

	public void resumeMedia()
	{
		if (!packValid)
		{
			loadPackPaused();
			return;
		}

		playbackClock.play();
		overlay.setPausedElapsedMs(playbackClock.getElapsedMilliseconds());
		overlay.resumePlayback();

		packStatus = "Playing.";
		updatePanelStatus();
	}

	public void restartMedia()
	{
		if (!validatePack(config.packFolderPath()))
		{
			packValid = false;
			playbackClock.pause();
			overlay.pausePlayback();
			overlay.forceReloadFrames();
			updatePanelStatus();
			return;
		}

		packValid = true;
		playbackClock.restart();
		overlay.resetPlayback();
		overlay.forceReloadFrames();
		overlay.resumePlayback();

		packStatus = "Restarted visuals.";
		updatePanelStatus();
	}

	public void reloadPack()
	{
		loadPackPaused();
	}

	public void seekByMs(long deltaMs)
	{
		if (!packValid)
		{
			return;
		}

		playbackClock.seekByMilliseconds(deltaMs);
		overlay.setPausedElapsedMs(playbackClock.getElapsedMilliseconds());

		packStatus = "Seeked to " + formatTime(playbackClock.getElapsedMilliseconds()) + ".";
		updatePanelStatus();
	}

	public void seekToMs(long targetMs)
	{
		if (!packValid)
		{
			return;
		}

		playbackClock.seekToMilliseconds(targetMs);
		overlay.setPausedElapsedMs(playbackClock.getElapsedMilliseconds());

		updatePanelStatus();
	}

	public void setFullScreen(boolean value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "fullScreen", value);
		updatePanelStatus();
	}

	public void setVideoAlignment(AdvancedSkyboxConfig.VideoAlignment value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "videoAlignment", value);
		updatePanelStatus();
	}

	public void setVideoWidthPercent(int value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "videoWidthPercent", clamp(value, 10, 150));
		updatePanelStatus();
	}

	public void setVideoHeightPercent(int value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "videoHeightPercent", clamp(value, 10, 100));
		updatePanelStatus();
	}

	public void setVideoOpacityPercent(int value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "videoOpacityPercent", clamp(value, 0, 100));
		updatePanelStatus();
	}

	public void setVideoXOffset(int value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "videoXOffset", clamp(value, -1000, 1000));
		updatePanelStatus();
	}

	public void setVideoYOffset(int value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "videoYOffset", clamp(value, -1000, 1000));
		updatePanelStatus();
	}

	public void setBottomFadePixels(int value)
	{
		configManager.setConfiguration(CONFIG_GROUP, "bottomFadePixels", clamp(value, 0, 400));
		updatePanelStatus();
	}

	public long getPlaybackElapsedMs()
	{
		return playbackClock.getElapsedMilliseconds();
	}

	public long getPlaybackLengthMs()
	{
		return overlay.getPlaybackLengthMs();
	}

	public boolean isMediaPlaying()
	{
		return playbackClock.isPlaying();
	}

	public String getCurrentPackFolderPath()
	{
		return config.packFolderPath();
	}

	public boolean isFullScreen()
	{
		return config.fullScreen();
	}

	public AdvancedSkyboxConfig.VideoAlignment getVideoAlignment()
	{
		return config.videoAlignment();
	}

	public int getVideoWidthPercent()
	{
		return config.videoWidthPercent();
	}

	public int getVideoHeightPercent()
	{
		return config.videoHeightPercent();
	}

	public int getVideoOpacityPercent()
	{
		return config.videoOpacityPercent();
	}

	public int getVideoXOffset()
	{
		return config.videoXOffset();
	}

	public int getVideoYOffset()
	{
		return config.videoYOffset();
	}

	public int getBottomFadePixels()
	{
		return config.bottomFadePixels();
	}

	private void loadPackPaused()
	{
		loadPackPausedFromPath(config.packFolderPath());
	}

	private void loadPackPausedFromPath(String packFolderPath)
	{
		packValid = validatePack(packFolderPath);

		playbackClock.resetPaused();
		overlay.pausePlayback();

		if (!packValid)
		{
			overlay.forceReloadFrames();
			log.warn("Invalid frame pack folder: {}", packStatus);
			updatePanelStatus();
			return;
		}

		overlay.resetPlayback();
		overlay.forceReloadFrames();

		packStatus = "Ready. Press Play.";
		updatePanelStatus();

		log.debug("Advanced Skybox frame pack loaded paused!");
	}

	private boolean validatePack(String packFolderPath)
	{
		if (packFolderPath == null || packFolderPath.trim().isEmpty())
		{
			packStatus = "No frame pack folder selected.";
			return false;
		}

		Path packFolder = Paths.get(packFolderPath.trim());

		if (!Files.exists(packFolder) || !Files.isDirectory(packFolder))
		{
			packStatus = "Frame pack folder not found.";
			return false;
		}

		Path framesFolder = packFolder.resolve("frames");

		if (!Files.exists(framesFolder) || !Files.isDirectory(framesFolder))
		{
			packStatus = "Missing frames folder.";
			return false;
		}

		long frameCount;

		try (Stream<Path> paths = Files.list(framesFolder))
		{
			frameCount = paths
					.filter(path ->
					{
						String name = path.getFileName().toString().toLowerCase();
						return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
					})
					.count();
		}
		catch (Exception e)
		{
			log.warn("Could not read frames folder: {}", framesFolder, e);
			packStatus = "Could not read frames folder.";
			return false;
		}

		if (frameCount <= 0)
		{
			packStatus = "No image frames found.";
			return false;
		}

		packStatus = "Frame pack valid: " + frameCount + " frames.";
		return true;
	}

	private void updatePanelStatus()
	{
		if (panel != null)
		{
			panel.updateStatus();
		}
	}

	private String formatTime(long ms)
	{
		long totalSeconds = Math.max(0, ms / 1000L);
		long minutes = totalSeconds / 60L;
		long seconds = totalSeconds % 60L;

		return String.format("%d:%02d", minutes, seconds);
	}

	private int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	private BufferedImage createSidebarIcon()
	{
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = icon.createGraphics();

		g.setColor(new Color(150, 235, 255, 70));
		g.drawRoundRect(1, 3, 12, 10, 4, 4);
		g.setColor(new Color(150, 235, 255, 40));
		g.drawRoundRect(0, 2, 14, 12, 5, 5);

		g.setColor(new Color(150, 235, 255, 90));
		g.drawLine(5, 3, 3, 0);
		g.drawLine(9, 3, 11, 0);

		g.setColor(new Color(92, 62, 40));
		g.fillRoundRect(2, 4, 10, 8, 3, 3);

		g.setColor(new Color(60, 40, 24));
		g.drawRoundRect(2, 4, 10, 8, 3, 3);

		g.setColor(new Color(170, 210, 220));
		g.fillRect(4, 6, 6, 4);

		g.setColor(new Color(245, 245, 245));
		g.drawLine(4, 6, 9, 9);
		g.drawLine(5, 6, 4, 7);
		g.drawLine(7, 6, 5, 8);
		g.drawLine(9, 6, 6, 9);

		g.setColor(new Color(235, 235, 235));
		g.drawLine(5, 3, 3, 0);
		g.drawLine(9, 3, 11, 0);

		g.setColor(new Color(40, 40, 40));
		g.fillOval(11, 7, 1, 1);
		g.fillOval(11, 9, 1, 1);

		g.setColor(new Color(60, 40, 24));
		g.drawLine(4, 12, 3, 14);
		g.drawLine(10, 12, 11, 14);

		g.dispose();

		return icon;
	}

	@Provides
	AdvancedSkyboxConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AdvancedSkyboxConfig.class);
	}
}
