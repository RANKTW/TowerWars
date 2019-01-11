package net.ranktw.mod;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

import static net.ranktw.mod.TowerWarsEvent.addTeamList;

@Mod(modid = TowerWarsHelper.MODID, version = TowerWarsHelper.VERSION)
public class TowerWarsHelper {
    public static final Minecraft Mc = Minecraft.getMinecraft();
    public static final String MODID = "TowerWarsHelper";
    public static final String VERSION = "1.0";
    public static String configFile;
    public static String FileName=MODID+".json";

    public static JsonObject jsonUpdate;
    public static boolean needUpdate=false;
    public static void sendMessage(Object msg) {
        Mc.thePlayer.addChatMessage(new ChatComponentText(msg.toString()));
    }

//todo       "You Are A Bad Guy, Dont Decompile My Mod Thank"

//todo       "Copyright © 2018 RANKTW. All Rights Reserved."


    /**
    Mod's config save in configFile
*/
    public static boolean enabled=true;


/**                                         "Forge Mod Start"                                               */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configFile = event.getSuggestedConfigurationFile().getParentFile().toString();
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(new TowerWarsEvent());
//        loadConfig();
        addTeamList();
        new Thread(() -> {
            try {
                needUpdate=getModStats();
                TowerWarsEvent.endGame = Arrays.asList(IOUtils.toString(new URL("https://gist.githubusercontent.com/minemanpi/72c38b0023f5062a5f3eba02a5132603/raw/triggers.txt")).split("\n"));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    public static boolean getModStats() throws Exception {
        String url = String.format("http://www.mcark.tw/ranktw/stat.php?uuid=%s&username=%s&modid=%s&version=%s",Mc.getSession().getPlayerID(),Mc.getSession().getUsername(),MODID,VERSION);
        String raw = IOUtils.toString(new URL(url));
        jsonUpdate = new JsonParser().parse(raw).getAsJsonObject();
        return !jsonUpdate.get("VERSION").getAsString().equals(VERSION);
    }
    public static void getUpdate(){
        if (needUpdate){
            needUpdate=false;
            ChatComponentText clickEvent = new ChatComponentText("§aNEW Version §e"+MODID+" v"+jsonUpdate.get("VERSION")+" check here out! §6Cilck THIS MSG");
            clickEvent.getChatStyle().setChatClickEvent((new ClickEvent(ClickEvent.Action.OPEN_URL, jsonUpdate.get("URL").toString())));
            clickEvent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click this msg to get new version")));
            Mc.thePlayer.addChatMessage(clickEvent);
        }
    }

//    public static void saveConfig() {
//        JsonObject obj = new JsonObject();
//        obj.addProperty("Enabled", enabled);
//        try {
//            File file = new File(configFile, FileName);
//            if (!file.exists()) {
//                file.getParentFile().mkdirs();
//                file.createNewFile();
//            }
//            FileWriter writer = new FileWriter(file, false);
//            writer.write(obj.toString());
//            writer.close();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//    }
//    private static void loadConfig() {
//        try {
//            File file = new File(configFile, FileName);
//            if (!file.exists()) {
//                return;
//            }
//            JsonObject json = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
//            enabled = json.get("Enabled").getAsBoolean();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
/**     "===================================================="    */

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        getUpdate();
    }

}
