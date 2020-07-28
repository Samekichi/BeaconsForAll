package top.theillusivec4.beaconsforall.core;

import com.google.common.base.Predicate;
import java.util.List;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import top.theillusivec4.beaconsforall.core.base.Accessor;
import top.theillusivec4.beaconsforall.core.base.ModConfig;

public class BeaconHooks {

  private static final Predicate<LivingEntity> VALID_CREATURE = living ->
      !(living instanceof PlayerEntity) && isValidCreature(living);

  private static boolean isValidCreature(LivingEntity livingEntity) {
    boolean validType = false;
    ModConfig config = BeaconsForAll.getInstance().getConfig();

    switch (config.getCreatureType()) {
      case TAMED:
        validType =
            livingEntity instanceof TameableEntity && ((TameableEntity) livingEntity).isTamed();
        break;
      case PASSIVE:
        validType = livingEntity instanceof AnimalEntity && !(livingEntity instanceof Monster);
        break;
      case ALL:
        validType = true;
        break;
    }
    boolean validConfig = config.getAdditionalCreatures().contains(livingEntity.getType());
    return validType || validConfig;
  }

  public static void addBeaconEffectsToCreatures(BeaconBlockEntity beacon) {
    int levels = beacon.getLevel();
    World world = beacon.getWorld();

    if (world == null || world.isClient()) {
      return;
    }
    Accessor accessor = BeaconsForAll.getInstance().getAccessor();
    StatusEffect primaryEffect = accessor.getPrimaryEffect(beacon);

    if (accessor.getBeamSegments(beacon).isEmpty() || levels <= 0 || primaryEffect == null) {
      return;
    }

    StatusEffect secondaryEffect = accessor.getSecondaryEffect(beacon);
    BlockPos pos = beacon.getPos();
    double d0 = levels * 10 + 10;
    int i = 0;

    if (levels >= 4 && primaryEffect == secondaryEffect) {
      i = 1;
    }

    int j = (9 + levels * 2) * 20;
    Box box = (new Box(pos)).expand(d0).stretch(0.0D, world.getHeight(), 0.0D);
    List<LivingEntity> list = world.getEntities(LivingEntity.class, box, VALID_CREATURE);

    for (LivingEntity entity : list) {
      entity.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
    }

    if (levels >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null) {

      for (LivingEntity entity : list) {
        entity.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, 0, true, true));
      }
    }
  }
}
