package arknights.entity.model;

import arknights.entity.ExusiaiEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExusiaiModel<T extends ExusiaiEntity> extends OperatorModel {
   private List<ModelRenderer> field_228286_w_ = Lists.newArrayList();
   private final ModelRenderer bipedHalo;
   private final ModelRenderer bipedWing;


   public ExusiaiModel(float modelSize ) {
      super(modelSize);

	  this.bipedHalo = new ModelRenderer(this, 64, 0);
	  this.bipedHalo.func_228301_a_(-4.0F, -9.0F, -4.0F, 8.0F, 1.0F, 8.0F, modelSize );
	  this.bipedHalo.setRotationPoint(this.bipedHead.rotationPointX, this.bipedHead.rotationPointY, this.bipedHead.rotationPointZ);

	  this.bipedWing = new ModelRenderer(this, 64, 16);
	  this.bipedWing.func_228301_a_(-3.0F, 0.0F, 0.0F, 28.0F, 16.0F, 1.0F, modelSize);
	  this.bipedWing.setRotationPoint(this.bipedBody.rotationPointX, this.bipedBody.rotationPointY, this.bipedBody.rotationPointZ);
	  /*
		Wing = new ModelRenderer(this);
		Wing.setRotationPoint(0.3326F, 5.4407F, 0.4995F);
		setRotationAngle(Wing, -0.0873F, 0.9599F, -0.1745F);

		RightWing = new ModelRenderer(this);
		RightWing.setRotationPoint(-1.4966F, -0.5688F, -7.4456F);
		setRotationAngle(RightWing, 0.0F, -2.0071F, 0.3491F);
		Wing.addChild(RightWing);
		RightWing.cubeList.add(new ModelBox(RightWing, 81, 47, 8.1639F, -2.8719F, -4.0539F, 0, 8, 9, 0.0F, true));

		LeftWing = new ModelRenderer(this);
		LeftWing.setRotationPoint(-0.6248F, -0.1343F, 4.7271F);
		setRotationAngle(LeftWing, -0.1745F, -3.0543F, 0.2618F);
		Wing.addChild(LeftWing);
		LeftWing.cubeList.add(new ModelBox(LeftWing, 63, 47, 0.2921F, -3.3063F, -5.2265F, 0, 8, 9, 0.0F, true));
		*/
   }


   @Override
    protected Iterable<ModelRenderer> func_225600_b_() {
        return Iterables.concat(super.func_225600_b_(), ImmutableList.of(this.bipedHalo, this.bipedWing));
    }

    public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        this.rightArmPose = BipedModel.ArmPose.EMPTY;
        this.leftArmPose = BipedModel.ArmPose.EMPTY;
        ItemStack itemstack = entityIn.getHeldItem(Hand.MAIN_HAND);
        if (itemstack.getItem() instanceof net.minecraft.item.BowItem && entityIn.isAggressive()) {
            if (entityIn.getPrimaryHand() == HandSide.RIGHT) {
                this.rightArmPose = BipedModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = BipedModel.ArmPose.BOW_AND_ARROW;
            }
        }

        super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
    }

    public void func_225599_a_(HandSide p_225599_1_, MatrixStack p_225599_2_) {
        float f = p_225599_1_ == HandSide.RIGHT ? 1.0F : -1.0F;
        ModelRenderer modelrenderer = this.getArmForSide(p_225599_1_);
        modelrenderer.rotationPointX += f;
        modelrenderer.func_228307_a_(p_225599_2_);
        modelrenderer.rotationPointX -= f;
        this.bipedHalo.copyModelAngles(this.bipedHeadwear);
        this.bipedWing.copyModelAngles(this.bipedBodyWear);
    }
}