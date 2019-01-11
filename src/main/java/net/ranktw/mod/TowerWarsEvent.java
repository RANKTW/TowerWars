package net.ranktw.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.ranktw.mod.TowerWarsHelper.Mc;
import static net.ranktw.mod.TowerWarsHelper.sendMessage;

public class TowerWarsEvent {//TOWERWARS
    private static DecimalFormat format = new DecimalFormat("###,###.###");
    static List<String> endGame = new ArrayList<>();
    public static List<String> teamList = new ArrayList<>();
    public static void addTeamList(){
        teamList.addAll(Arrays.asList("RED\nBLUE\nGREEN\nYELLOW\nGOLD\nAQUA".split("\n")));
        endGame.add("Winners");
    }

    private static String tower="";
    private static String gold="-";
    private static String income="";
    private static String nextincome="";

    private int team;

    @SubscribeEvent
    public void RenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (!(TowerWarsHelper.enabled && inTowerWars()))return;
        GlStateManager.pushMatrix();
        updateStats();
        FontRenderer renderer = Mc.fontRendererObj;
        renderer.drawStringWithShadow(gold+"   "+income,event.resolution.getScaledWidth()/2-renderer.getStringWidth(gold+"   "+income)/2,10,-1);
        renderer.drawStringWithShadow(nextincome,event.resolution.getScaledWidth()/2-renderer.getStringWidth(nextincome)/2,10+10,-1);
        renderer.drawStringWithShadow(tower,event.resolution.getScaledWidth()/2-renderer.getStringWidth(tower)/2,1,-1);
       GlStateManager.popMatrix();
    }
    @SubscribeEvent
    public void guiEvent(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!(TowerWarsHelper.enabled && inTowerWars()))return;
        GuiScreen gui = event.gui;
        if (gui instanceof GuiChest) {
            int x = Mc.displayWidth/2/2 - 45 - 18- 18;
            int y = Mc.displayHeight/2/2- 50- 18+1;
            GuiChest guiChest = (GuiChest)Mc.currentScreen;
            IInventory inventory = guiChest.inventorySlots.getSlot(0).inventory;
            if (inventory.getName().equals("Build Tower")||inventory.getName().equals("Summon Monster")) {
                int h = 0; y+=inventory.getName().equals("Build Tower")?-9-18:0;
                for (Slot slot:guiChest.inventorySlots.inventorySlots){
                    if (slot.getHasStack()&&checkCost(slot)!=-1){
                        boolean ignore = inventory.getName().equals("Summon Monster")&&slot.getStack().getItem().equals(Item.getItemById(166));
                        boolean enchanted = slot.getStack().isItemEnchanted();
                        boolean b = getGold()>=checkCost(slot)&&!ignore;
                        Color color = b?enchanted?new Color(0xCB9113):new Color(0x8FE83A):new Color(0xBF1912);
                        drawRect(x, y, x + 18, y + 18,color.getRGB());
                    }
                    h++;
                    x+=18;
                    if (h>8){
                        h=0;
                        y+=18;
                        x=Mc.displayWidth/2/2 - 45 - 18- 18;
                    }
                }
            }
        }
    }
    public static void drawRect(int left, int top, int right, int bottom, int color){
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public static int getColorWithAlpha(final int rgb, final int a) {
        final int r = rgb >> 16 & 0xFF;
        final int g = rgb >> 8 & 0xFF;
        final int b = rgb & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event){
        String message = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        if (message.contains("placed the following tower")){
            String s = message.split("\\(")[1].replace("/80)","");
            tower= String.format("§7Tower (§f%s§7/§f80§7)",s);
        }
        if (message.contains("Winners")) {
            restart();
            sendMessage("/ac gg");
            return;
        }
        if (endGame.stream().anyMatch(message::contains) && message.startsWith(" ")) {
            restart();
        }
    }

    @SubscribeEvent
    public void onNewGame(EntityJoinWorldEvent event){
        if (event.entity.equals(Mc.thePlayer)){
            restart();
        }
    }

    public static boolean inTowerWars(){
        return getGameModeString().contains("TOWERWARS");
    }
    public static String getGameModeString() {
        if (Mc.thePlayer==null)return "";
        Scoreboard scoreboard = Mc.thePlayer.getWorldScoreboard();
        if (scoreboard != null) {
            ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
            if (scoreObjective != null && scoreObjective.getDisplayName() != null) {
                return EnumChatFormatting.getTextWithoutFormattingCodes(scoreObjective.getDisplayName()).replaceAll(" ","_");
            }
        }
        return "";
    }
    public static List<String> getScoreboardList(){
        List<String> list = new ArrayList();
        if (Mc.thePlayer == null) return list;
        Scoreboard scoreboard = Mc.thePlayer.getWorldScoreboard();
        if (scoreboard == null) return list;
        for (ScorePlayerTeam s : scoreboard.getTeams()) {
            if (s.getRegisteredName().startsWith("team")) {
                String gamemode = EnumChatFormatting.getTextWithoutFormattingCodes(s.getColorPrefix()+s.getColorSuffix());
                list.add(gamemode);
            }
        }
        return list;
    }
    public void updateStats() {
        team = 0;
        try {
            for (String s : getScoreboardList()) {
                if (s.contains("Gold: ")){
                    gold = "§6Gold: §r" + format.format(Integer.parseInt(s.replaceAll("[^0-9]", "")));
                }
                if (s.contains("Income: ")) income = "§6Income: §r" + format.format(Integer.parseInt(s.replaceAll("[^0-9]", "")));
                if (s.contains("income"))nextincome = "§7Next income: §r" + getNextIncome(s.replaceAll("[^0-9]", ""));
            }
        }catch (Exception e){e.printStackTrace();}
    }
    public String getNextIncome(String s){
        try {
            int i = Integer.parseInt(s);
            return (i > 3 ? "§f" : "§c") + s;
        }catch (NumberFormatException e){
            return s;
        }
    }
    public int checkCost(Slot slot){
        Optional<String> s=slot.getStack().getTooltip(Mc.thePlayer,false).stream().filter(gold->gold.contains("Cost")).findAny();
        return s.map(s1 -> Integer.parseInt(EnumChatFormatting.getTextWithoutFormattingCodes(s1).replaceAll("[^0-9]", ""))).orElse(-1);
    }
    public int getGold(){
        return Integer.parseInt(EnumChatFormatting.getTextWithoutFormattingCodes(gold).replaceAll("[^0-9]",""));
    }
    public void restart(){
        tower="";
        gold="-";
        income="";
        nextincome="";
    }
}
