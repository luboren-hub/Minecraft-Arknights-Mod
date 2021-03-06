package arknights.entity.enemy;

import arknights.entity.operator.OperatorBase;
import arknights.item.FaustCrossBow;
import arknights.registry.ItemHandler;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraft.world.raid.Raid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FaustEntity extends CrossBowEnemy {
    private static final DataParameter<Boolean> DATA_CHARGING_STATE = EntityDataManager.createKey(FaustEntity.class, DataSerializers.BOOLEAN);
    private final Inventory inventory = new Inventory(5);
    protected Raid raid;

    public FaustEntity(EntityType<? extends FaustEntity> p_i50198_1_, World p_i50198_2_) {
        super(p_i50198_1_, p_i50198_2_);
    }

    public void registerGoals() {
        super.registerGoals();
    }

    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.35F);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(20.0D);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
    }

    protected void registerData() {
        super.registerData();
        this.dataManager.register(DATA_CHARGING_STATE, false);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isCharging() {
        return this.dataManager.get(DATA_CHARGING_STATE);
    }

    public void setCharging(boolean p_213671_1_) {
        this.dataManager.set(DATA_CHARGING_STATE, p_213671_1_);
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        ListNBT listnbt = new ListNBT();

        for(int i = 0; i < this.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.inventory.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                listnbt.add(itemstack.write(new CompoundNBT()));
            }
        }

        compound.put("Inventory", listnbt);
    }

    @OnlyIn(Dist.CLIENT)
    public CrossBowEnemy.ArmPose getArmPose() {
        if (this.isCharging()) {
            return CrossBowEnemy.ArmPose.CROSSBOW_CHARGE;
        } else if (this.isHolding(Items.CROSSBOW)) {
            return CrossBowEnemy.ArmPose.CROSSBOW_HOLD;
        } else {
            return this.isAggressive() ? CrossBowEnemy.ArmPose.ATTACKING : CrossBowEnemy.ArmPose.NEUTRAL;
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        ListNBT listnbt = compound.getList("Inventory", 10);

        for(int i = 0; i < listnbt.size(); ++i) {
            ItemStack itemstack = ItemStack.read(listnbt.getCompound(i));
            if (!itemstack.isEmpty()) {
                this.inventory.addItem(itemstack);
            }
        }

        this.setCanPickUpLoot(true);
    }

    public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn) {
        Block block = worldIn.getBlockState(pos.down()).getBlock();
        return block != Blocks.GRASS_BLOCK && block != Blocks.SAND ? 0.5F - worldIn.getBrightness(pos) : 10.0F;
    }

    /**
     * Will return how many at most can spawn in a chunk at once.
     */
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    @Nullable
    public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        this.setEquipmentBasedOnDifficulty(difficultyIn);
        this.setEnchantmentBasedOnDifficulty(difficultyIn);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        ItemStack itemstack = new ItemStack(ItemHandler.FAUST_CROSSBOW);
        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, itemstack);
    }

    /**
     * Returns whether this Entity is on the same team as the given Entity.
     */
    public boolean isOnSameTeam(Entity entityIn) {
        if (super.isOnSameTeam(entityIn)) {
            return true;
        } else if (entityIn instanceof LivingEntity && ((LivingEntity)entityIn).getCreatureAttribute() == CreatureAttribute.ILLAGER) {
            return this.getTeam() == null && entityIn.getTeam() == null;
        } else {
            return false;
        }
    }


    /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
        Hand hand = ProjectileHelper.getHandWith(this, Items.CROSSBOW);
        ItemStack itemstack = this.getHeldItem(hand);
        if (this.isHolding(ItemHandler.FAUST_CROSSBOW)) {
            FaustCrossBow.fireProjectiles(this.world, this, hand, itemstack, 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
        }

        this.idleTime = 0;
    }

    public void shoot(LivingEntity p_213670_1_, ItemStack p_213670_2_, IProjectile p_213670_3_, float p_213670_4_) {
        Entity entity = (Entity)p_213670_3_;
        double d0 = p_213670_1_.func_226277_ct_() - this.func_226277_ct_();
        double d1 = p_213670_1_.func_226281_cx_() - this.func_226281_cx_();
        double d2 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1);
        double d3 = p_213670_1_.func_226283_e_(0.3333333333333333D) - entity.func_226278_cu_() + d2 * (double)0.2F;
        Vector3f vector3f = this.func_213673_a(new Vec3d(d0, d3, d1), p_213670_4_);
        p_213670_3_.shoot((double)vector3f.getX(), (double)vector3f.getY(), (double)vector3f.getZ(), 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
    }

    private Vector3f func_213673_a(Vec3d p_213673_1_, float p_213673_2_) {
        Vec3d vec3d = p_213673_1_.normalize();
        Vec3d vec3d1 = vec3d.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
        if (vec3d1.lengthSquared() <= 1.0E-7D) {
            vec3d1 = vec3d.crossProduct(this.func_213286_i(1.0F));
        }

        Quaternion quaternion = new Quaternion(new Vector3f(vec3d1), 90.0F, true);
        Vector3f vector3f = new Vector3f(vec3d);
        vector3f.func_214905_a(quaternion);
        Quaternion quaternion1 = new Quaternion(vector3f, p_213673_2_, true);
        Vector3f vector3f1 = new Vector3f(vec3d);
        vector3f1.func_214905_a(quaternion1);
        return vector3f1;
    }
}