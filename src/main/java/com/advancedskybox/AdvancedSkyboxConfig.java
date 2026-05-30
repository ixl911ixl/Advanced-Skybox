package com.advancedskybox;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("advancedskybox")
public interface AdvancedSkyboxConfig extends Config
{
	enum VideoAlignment
	{
		LEFT,
		CENTER,
		RIGHT
	}

	@ConfigItem(
			keyName = "sidebarControlsNotice",
			name = "Controls Location",
			description = "Video selection and playback controls are handled in the Advanced Skybox side panel.",
			position = 0
	)
	default String sidebarControlsNotice()
	{
		return "Use the Advanced Skybox side-panel icon to select a local frame pack and control visual playback.";
	}

	@ConfigItem(
			keyName = "packFolderPath",
			name = "Frame Pack Folder",
			description = "Folder containing a frames folder with extracted image frames",
			position = 1,
			hidden = true
	)
	default String packFolderPath()
	{
		return "";
	}

	@ConfigItem(
			keyName = "fullScreen",
			name = "Full Screen",
			description = "Draw the video across the full game viewport",
			position = 2,
			hidden = true
	)
	default boolean fullScreen()
	{
		return false;
	}

	@ConfigItem(
			keyName = "videoAlignment",
			name = "Alignment",
			description = "Horizontal placement of the video layer",
			position = 3,
			hidden = true
	)
	default VideoAlignment videoAlignment()
	{
		return VideoAlignment.CENTER;
	}

	@Range(min = 10, max = 150)
	@ConfigItem(
			keyName = "videoWidthPercent",
			name = "Video Width %",
			description = "How wide the video layer is",
			position = 4,
			hidden = true
	)
	default int videoWidthPercent()
	{
		return 100;
	}

	@Range(min = 10, max = 100)
	@ConfigItem(
			keyName = "videoHeightPercent",
			name = "Video Height %",
			description = "How tall the video layer is",
			position = 5,
			hidden = true
	)
	default int videoHeightPercent()
	{
		return 30;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
			keyName = "videoOpacityPercent",
			name = "Video Opacity %",
			description = "Opacity of the video layer",
			position = 6,
			hidden = true
	)
	default int videoOpacityPercent()
	{
		return 80;
	}

	@Range(min = -1000, max = 1000)
	@ConfigItem(
			keyName = "videoXOffset",
			name = "Video X Offset px",
			description = "Moves the video left or right",
			position = 7,
			hidden = true
	)
	default int videoXOffset()
	{
		return 0;
	}

	@Range(min = -1000, max = 1000)
	@ConfigItem(
			keyName = "videoYOffset",
			name = "Video Y Offset px",
			description = "Moves the video up or down",
			position = 8,
			hidden = true
	)
	default int videoYOffset()
	{
		return 0;
	}

	@Range(min = 0, max = 400)
	@ConfigItem(
			keyName = "bottomFadePixels",
			name = "Bottom Fade px",
			description = "How softly the video fades at the bottom edge",
			position = 9,
			hidden = true
	)
	default int bottomFadePixels()
	{
		return 90;
	}
}
