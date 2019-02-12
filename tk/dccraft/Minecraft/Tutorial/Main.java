package tk.dccraft.Minecraft.Tutorial;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import tk.dccraft.Minecraft.Tutorial.handlers.RegistryHandler;

@Mod(modid = "tut_warp_mod", name = "Tutorial Warp Mod", version = "0.0.1", acceptableRemoteVersions = "*")
public class Main {
	
	@Instance
	public static Main instance;
	
	@EventHandler
	public static void serverInit(FMLServerStartingEvent event){
		RegistryHandler.serverRegistries(event);
	}

}
