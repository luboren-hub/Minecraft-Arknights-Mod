package arknights.entity.ai;

import arknights.entity.operator.MedicSingle;
import arknights.entity.operator.OperatorBase;
import arknights.registry.EntityHandler;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class MedicSingleHealTarget<T extends LivingEntity> extends TargetGoal {
    //protected final Class<T> targetClass;
    protected final int targetChance;
    protected LivingEntity nearestTarget;
    private List<LivingEntity> targets = new ArrayList<>();
    /** This filter is applied to the Entity search. Only matching entities will be targeted. */
    protected EntityPredicate targetEntitySelector;

    public MedicSingleHealTarget(MobEntity p_i50313_1_, boolean p_i50313_3_) {
        this(p_i50313_1_, p_i50313_3_, false);
    }

    public MedicSingleHealTarget(MobEntity p_i50314_1_, /*Class<T> p_i50314_2_,*/ boolean p_i50314_3_, boolean p_i50314_4_) {
        this(p_i50314_1_, 10, p_i50314_3_, p_i50314_4_, (Predicate<LivingEntity>)null);
    }

    public MedicSingleHealTarget(MobEntity p_i50315_1_, /*Class<T> p_i50315_2_, */ int p_i50315_3_, boolean p_i50315_4_, boolean p_i50315_5_, @Nullable Predicate<LivingEntity> p_i50315_6_) {
        super(p_i50315_1_, p_i50315_4_, p_i50315_5_);
        //this.targetClass = p_i50315_2_;
        this.targetChance = p_i50315_3_;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetEntitySelector = (new EntityPredicate()).setDistance(16).setCustomPredicate(p_i50315_6_);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.goalOwner.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.findNearestTarget();
            return this.nearestTarget != null;
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.goalOwner.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    protected void findNearestTarget() {
        float hp = 0;
        int i = 0;
        int n = 0;
        if(!this.goalOwner.world.<T>getEntitiesWithinAABB((Class<? extends T>) PlayerEntity.class, this.getTargetableArea(16), (Predicate<T>)null).isEmpty()){
          for(T entity : this.goalOwner.world.<T>getEntitiesWithinAABB((Class<? extends T>) PlayerEntity.class, this.getTargetableArea(16), (Predicate<T>)null)) {
             if (entity == ((MedicSingle) this.goalOwner).getOwner()) {
                    this.targets.add(entity);
              }
            }
        }
        if(!this.goalOwner.world.<T>getEntitiesWithinAABB((Class<? extends T>) OperatorBase.class, this.getTargetableArea(16), (Predicate<T>)null).isEmpty()) {
            for (T entity : this.goalOwner.world.<T>getEntitiesWithinAABB((Class<? extends T>) OperatorBase.class, this.getTargetableArea(16), (Predicate<T>) null)) {
                if (entity != null) {
                    if (((OperatorBase) entity).getOwner() == ((MedicSingle) this.goalOwner).getOwner()) {
                        this.targets.add(entity);
                    }
                }
            }
        }
        if(this.targets.size() > 0) {
            for (LivingEntity entity : this.targets) {
                if (entity.getHealth() > hp) {
                    hp = entity.getHealth();
                    n = i;
                }
                i++;
            }
            this.nearestTarget = this.targets.get(n);
        } else {
            this.nearestTarget = null;
        }
        /*
        if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
            this.nearestTarget = this.goalOwner.world.<T>func_225318_b(this.targetClass, this.targetEntitySelector, this.goalOwner, this.goalOwner.func_226277_ct_(), this.goalOwner.func_226280_cw_(), this.goalOwner.func_226281_cx_(), this.getTargetableArea(this.getTargetDistance()));
        } else {
            this.nearestTarget = this.goalOwner.world.getClosestPlayer(this.targetEntitySelector, this.goalOwner, this.goalOwner.func_226277_ct_(), this.goalOwner.func_226280_cw_(), this.goalOwner.func_226281_cx_());
        }

         */

    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.nearestTarget);
        super.startExecuting();
    }
}