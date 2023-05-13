package net.fabricmc.fabric.impl.client.indigo;

import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class IndigoMixinConfigPlugin implements IMixinConfigPlugin {
    /** Set by other renderers to disable loading of Indigo. */
    //private static final String JSON_KEY_DISABLE_INDIGO = "fabric-renderer-api-v1:contains_renderer";
    /**
     * Disables vanilla block tesselation and ensures vertex format compatibility.
     */
    //private static final String JSON_KEY_FORCE_COMPATIBILITY = "fabric-renderer-indigo:force_compatibility";

    //private static boolean needsLoad = true;

    private static final boolean indigoApplicable = true;
    private static final boolean forceCompatibility = false;
/*
    private static void loadIfNeeded() {
        if (needsLoad) {
            for (ModInfo modInfo : FMLLoader.getLoadingModList().getMods()) {
                if (ModList.get().isLoaded("rubidium")) {
                    indigoApplicable = false;
                }
            }

            for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
                final ModMetadata meta = container.getMetadata();

                if (meta.containsCustomValue(JSON_KEY_DISABLE_INDIGO)) {
                    indigoApplicable = false;
                } else if (meta.containsCustomValue(JSON_KEY_FORCE_COMPATIBILITY)) {
                    forceCompatibility = true;
                }
            }

            needsLoad = false;
        }
    }


    static boolean shouldApplyIndigo() {
        loadIfNeeded();
        return indigoApplicable;
    }

    static boolean shouldForceCompatibility() {
        loadIfNeeded();
        return forceCompatibility;
    }

 */

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        //return shouldApplyIndigo();
        return ModList.get().isLoaded("rubidium") ? mixinClassName.contains("link.infra.indium") : mixinClassName.contains("net.fabricmc.fabric.mixin.client.indigo");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
