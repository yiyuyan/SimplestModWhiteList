package cn.ksmcbrigade.smwl;

import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mod("smwl")
public class SimplestModWhiteList {

    ArrayList<String> allowed = new ArrayList<>();
    File file = new File("config/smwl-config.json");

    public SimplestModWhiteList() throws IOException {
        MinecraftForge.EVENT_BUS.register(this);
        if(!file.exists()){
            FileUtils.writeStringToFile(file,"[]");
        }
        JsonParser.parseString(FileUtils.readFileToString(file)).getAsJsonArray().forEach(k->allowed.add(k.getAsString()));
    }

    @SubscribeEvent
    public void onPlayerLoginIn(PlayerEvent.PlayerLoggedInEvent event){
        ArrayList<String> CLIENT_MODS = new ArrayList<>(Objects.requireNonNull(NetworkHooks.getConnectionData(((ServerPlayer) event.getEntity()).connection.connection)).getModList());
        ArrayList<String> REAL_CLIENT_MODS = new ArrayList<>(CLIENT_MODS);
        REAL_CLIENT_MODS.removeAll(allowed);
        List<String> SERVER_MODS = new ArrayList<>();
        ModList.get().getMods().stream().filter(this::notServer).toList().forEach(m->SERVER_MODS.add(m.getModId()));
        if(!Arrays.toString(REAL_CLIENT_MODS.toArray()).equalsIgnoreCase(Arrays.toString(SERVER_MODS.toArray()))){
            ((ServerPlayer) event.getEntity()).connection.disconnect(Component.literal("The server mods list does not match your client mods list,please modify your mods list and try joining the server again.").withStyle(style -> style.withColor(ChatFormatting.RED)).append(CommonComponents.NEW_LINE).append(Component.literal("Server mods list: ").withStyle(style -> style.withColor(ChatFormatting.RESET).withColor(ChatFormatting.WHITE)).append(Arrays.toString(SERVER_MODS.toArray())).append(CommonComponents.NEW_LINE).append(Component.literal("Client mods list: ")).append(Arrays.toString(CLIENT_MODS.toArray())).append(CommonComponents.NEW_LINE).append(Component.literal("Allowed mods: "+Arrays.toString(allowed.toArray())))));
        }
    }

    public boolean notServer(IModInfo info){
        for (IModInfo.ModVersion dependency : info.getDependencies()) {
            if(dependency.getSide().equals(IModInfo.DependencySide.SERVER)){
                return false;
            }
        }
        return true;
    }
}
