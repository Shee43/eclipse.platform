package org.eclipse.update.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import org.eclipse.core.runtime.*;
import java.io.File;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import java.net.URL;
import org.eclipse.swt.widgets.Display;

/**
 * Bundle of all images used by the PDE plugin.
 */
public class UpdateUIPluginImages {

	private static final String NAME_PREFIX= UpdateUIPlugin.getPluginId()+".";
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	private final static URL BASE_URL = UpdateUIPlugin.getDefault().getDescriptor().getInstallURL();

	private static ImageRegistry PLUGIN_REGISTRY;
	
	public final static String ICONS_PATH;
	static {
		Display display = Display.getCurrent();
		if (display==null) {
			ICONS_PATH = "icons/full/";
		}
		else {
			if(display.getIconDepth() > 4)
				ICONS_PATH = "icons/full/";//$NON-NLS-1$
			else
				ICONS_PATH = "icons/basic/";//$NON-NLS-1$
		}
	}

	/**
	 * Set of predefined Image Descriptors.
	 */
	
	private static final String PATH_OBJ= ICONS_PATH+"obj16/";
	private static final String PATH_VIEW = ICONS_PATH+"view16/";
	private static final String PATH_LCL= ICONS_PATH+"elcl16/";
	private static final String PATH_LCL_HOVER= ICONS_PATH+"clcl16/";
	private static final String PATH_LCL_DISABLED= ICONS_PATH+"dlcl16/";
	private static final String PATH_TOOL = ICONS_PATH + "etool16/";
	private static final String PATH_TOOL_HOVER = ICONS_PATH + "ctool16/";
	private static final String PATH_TOOL_DISABLED = ICONS_PATH + "dtool16/";
	private static final String PATH_OVR = ICONS_PATH + "ovr16/";
	private static final String PATH_WIZBAN = ICONS_PATH + "wizban/";

	/**
	 * Frequently used images
	 */
	public static final String IMG_FORM_BANNER = NAME_PREFIX+"FORM_BANNER";

	/**
	 * OBJ16
	 */
	//public static final ImageDescriptor DESC_ALL_SC_OBJ    = create(PATH_OBJ, "all_sc_obj.gif");
	
	/**
	 * OVR16
	 */
	//public static final ImageDescriptor DESC_DOC_CO   = create(PATH_OVR, "doc_co.gif");

	/**
	 * TOOL16
	 */
	//public static final ImageDescriptor DESC_DEFCON_TOOL = create(PATH_TOOL, "defcon_wiz.gif");

	/**
	 * LCL
	 */
	//public static final ImageDescriptor DESC_ADD_ATT = create(PATH_LCL, "add_att.gif");

	/**
	 * WIZ
	 */
	public static final ImageDescriptor DESC_FORM_BANNER  = create(PATH_WIZBAN, "form_banner.gif");

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
	}

	public static Image get(String key) {
		if (PLUGIN_REGISTRY==null) initialize();
		return PLUGIN_REGISTRY.get(key);
	}

public static ImageDescriptor getImageDescriptorFromPlugin(
	IPluginDescriptor pluginDescriptor, 
	String subdirectoryAndFilename) {
	URL installURL = pluginDescriptor.getInstallURL();
	try {
		URL newURL = new URL(installURL, subdirectoryAndFilename);
		return ImageDescriptor.createFromURL(newURL);
	}
	catch (MalformedURLException e) {
	}
	return null;
}

public static Image getImageFromPlugin(
	IPluginDescriptor pluginDescriptor,
	String subdirectoryAndFilename) {
	URL installURL = pluginDescriptor.getInstallURL();
	Image image = null;
	try {
		URL newURL = new URL(installURL, subdirectoryAndFilename);
		String key = newURL.toString();
		if (PLUGIN_REGISTRY==null) initialize();
		image = PLUGIN_REGISTRY.get(key);
		if (image==null) {
			ImageDescriptor desc = ImageDescriptor.createFromURL(newURL);
			image = desc.createImage();
			PLUGIN_REGISTRY.put(key, image);
		}
	}
	catch (MalformedURLException e) {
	}
	return image;
}
/* package */
private static final void initialize() {
	PLUGIN_REGISTRY = new ImageRegistry();
	manage(IMG_FORM_BANNER, DESC_FORM_BANNER);

}

private static URL makeImageURL(String prefix, String name) {
	String path = prefix + name;
	URL url = null;
	try {
		url = new URL(BASE_URL, path);
	}
	catch (MalformedURLException e) {
		return null;
	}
	return url;
}

public static Image manage(String key, ImageDescriptor desc) {
	Image image = desc.createImage();
	PLUGIN_REGISTRY.put(key, image);
	return image;
}
}
