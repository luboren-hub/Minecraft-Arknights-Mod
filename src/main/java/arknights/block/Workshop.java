package arknights.block;

import arknights.container.WorkshopContainer;
import arknights.tileentity.TradingHomeEntity;
import arknights.tileentity.WorkshopEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class Workshop extends Block {
    private static final ITextComponent field_220271_a = new TranslationTextComponent("workshop");
    public Workshop(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    //@Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return new WorkshopEntity();
    }

    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result){
        //
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof WorkshopEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (WorkshopEntity) tileEntity, pos);
            }
        }
        //
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity != null) {
                tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
                    if (inv == null) {
                        return;
                    }
                    for (int i = 0; i < inv.getSlots(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            ItemEntity ent = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ());
                            ent.setItem(stack);
                            worldIn.addEntity(ent);
                        }
                    }
                });
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        /*
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity != null && tileentity instanceof WorkshopEntity) {
                for(int i = 0; i < 3; i++){
                    ItemStack stack = ((WorkshopEntity)tileentity).inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        ItemEntity ent = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ());
                        ent.setItem(stack);
                        worldIn.addEntity(ent);
                    }
                }
            }
            /*
            if (tileentity != null) {
                tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
                    if (inv == null) {
                        return;
                    }
                    for (int i = 0; i < inv.getSlots(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            ItemEntity ent = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ());
                            ent.setItem(stack);
                            worldIn.addEntity(ent);
                        }
                    }
                });
            }*/
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
}
