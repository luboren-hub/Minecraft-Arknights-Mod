package arknights.entity.operator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class MeleeOperator extends OperatorBase {
    public MeleeOperator(EntityType<? extends TameableEntity> p_i48574_1_, World p_i48574_2_){
        super(p_i48574_1_, p_i48574_2_);
    }

    public MeleeOperator(EntityType<? extends TameableEntity> p_i48574_1_, World p_i48574_2_, Item item) {
        super(p_i48574_1_, p_i48574_2_, item);
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag =true;
        if (!(entityIn instanceof OperatorBase) && entityIn != this.getOwner()) {
            flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 5);
        }
        return flag;
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
    }
}
