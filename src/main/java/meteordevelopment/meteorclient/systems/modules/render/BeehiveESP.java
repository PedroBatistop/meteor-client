/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BeehiveESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> beehives = sgGeneral.add(new BoolSetting.Builder()
        .name("beehives")
        .description("Highlights crafted beehives.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> beeNests = sgGeneral.add(new BoolSetting.Builder()
        .name("bee-nests")
        .description("Highlights naturally generated bee nests.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How beehives and bee nests are highlighted.")
        .defaultValue(ShapeMode.Lines)
        .build()
    );

    private final Setting<SettingColor> beehiveColor = sgGeneral.add(new ColorSetting.Builder()
        .name("beehive-color")
        .description("Outline color for crafted beehives.")
        .defaultValue(new SettingColor(255, 205, 50, 255))
        .build()
    );

    private final Setting<SettingColor> beeNestColor = sgGeneral.add(new ColorSetting.Builder()
        .name("bee-nest-color")
        .description("Outline color for naturally generated bee nests.")
        .defaultValue(new SettingColor(255, 145, 45, 255))
        .build()
    );

    private final Setting<Integer> fillOpacity = sgGeneral.add(new IntSetting.Builder()
        .name("fill-opacity")
        .description("Opacity of the box fill.")
        .defaultValue(50)
        .range(0, 255)
        .sliderMax(255)
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .build()
    );

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draws tracers to highlighted beehives and bee nests.")
        .defaultValue(false)
        .build()
    );

    private final Color sideColor = new Color();
    private int count;

    public BeehiveESP() {
        super(Categories.Render, "beehive-esp", "Highlights beehives and bee nests through walls.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        count = 0;

        if (!beehives.get() && !beeNests.get()) return;

        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (!(blockEntity instanceof BeehiveBlockEntity)) continue;

            Block block = blockEntity.getBlockState().getBlock();
            if (block == Blocks.BEEHIVE && !beehives.get()) continue;
            if (block == Blocks.BEE_NEST && !beeNests.get()) continue;
            if (block != Blocks.BEEHIVE && block != Blocks.BEE_NEST) continue;

            BlockPos pos = blockEntity.getBlockPos();
            Color lineColor = block == Blocks.BEEHIVE ? beehiveColor.get() : beeNestColor.get();

            sideColor.set(lineColor).a(fillOpacity.get());
            event.renderer.box(pos, sideColor, lineColor, shapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(
                    RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    lineColor
                );
            }

            count++;
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }
}
