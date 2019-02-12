package tk.dccraft.Minecraft.Tutorial.handlers;

import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import tk.dccraft.Minecraft.Tutorial.commands.WarpCommand;

public class RegistryHandler {
	
	public static void serverRegistries(FMLServerStartingEvent event){
		event.registerServerCommand(new WarpCommand());
	}

}
