package org.jme.filter;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

/**
 * The ColorScale filter applys a color to the render image. You can use this filter to tint the render according to one
 * particular color without change any material (underwater scene, night scene, fire scene) or to achieve
 * fade-in/fade-out effect.
 *
 * <pre>
 * Features:
 * 	- Allow to set the color to apply. Default is red.
 * 	- Allow to set intensity of the color. Default is 0.7f. Frag shader clamps color intensity between 0 and 1.
 * </pre>
 *
 * @author H
 */
public class ColorScaleFilter extends Filter {

    /**
     * Default values
     */
    private static final ColorRGBA DEAFULT_COLOR = ColorRGBA.Red.clone();
    private static final float DEFAULT_DENSITY = 0.7f;

    private ColorRGBA filterColor = null;
    private float colorDensity = 0f;

    /**
     * Default Constructor.
     */
    public ColorScaleFilter() {
        this(ColorScaleFilter.DEAFULT_COLOR, ColorScaleFilter.DEFAULT_DENSITY);
    }

    /**
     * Constructor.
     *
     * @param filterColor  Allow to set the color to apply. Default is red.
     * @param colorDensity Allow to set intensity of the color. Frag shader clamps color intensity between 0 and 1.
     */
    public ColorScaleFilter(final ColorRGBA filterColor, final float colorDensity) {
        super("ColorScaleFilter");
        this.filterColor = filterColor;
        this.colorDensity = colorDensity;
    }

    /**
     * @see com.jme3.post.Filter#getMaterial()
     */
    @Override
    protected Material getMaterial() {
        return this.material;
    }

    public void setOverlay(boolean overlay) {
        this.material.setBoolean("Overlay", overlay);
    }

    public void setMultiply(boolean multiply) {
        this.material.setBoolean("Multiply", multiply);
    }

    /**
     * @see com.jme3.post.Filter#initFilter(com.jme3.asset.AssetManager, com.jme3.renderer.RenderManager,
     * com.jme3.renderer.ViewPort, int, int)
     */
    @Override
    protected void initFilter(final AssetManager manager, final RenderManager renderManager, final ViewPort vp,
                              final int w, final int h) {
        this.material = new Material(manager, "ShaderBlow/MatDefs/Filters/ColorScale/ColorScale.j3md");
        this.material.setColor("FilterColor", this.filterColor);
        this.material.setFloat("ColorDensity", this.colorDensity);
    }

    public void setColorDensity(final float colorDensity) {
        if (this.material != null) {
            this.material.setFloat("ColorDensity", this.colorDensity);
            this.colorDensity = colorDensity;
        }
    }

    public float getColorDensity() {
        return this.colorDensity;
    }

    public void setFilterColor(final ColorRGBA filterColor) {
        if (this.material != null) {
            this.material.setColor("FilterColor", this.filterColor);
            this.filterColor = filterColor;
        }
    }

    public ColorRGBA getFilterColor() {
        return this.filterColor;
    }

}