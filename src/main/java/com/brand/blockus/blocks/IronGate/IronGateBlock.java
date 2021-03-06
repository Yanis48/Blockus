package com.brand.blockus.blocks.IronGate;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class IronGateBlock extends Block {
	   public static final DirectionProperty FACING;
	   public static final BooleanProperty OPEN;
	   public static final EnumProperty<DoorHinge> HINGE;
	   public static final BooleanProperty POWERED;
	   public static final EnumProperty<DoubleBlockHalf> HALF;
	   protected static final VoxelShape NORTH_SHAPE;
	   protected static final VoxelShape SOUTH_SHAPE;
	   protected static final VoxelShape EAST_SHAPE;
	   protected static final VoxelShape WEST_SHAPE;

	   protected IronGateBlock(Block.Settings settings) {
	      super(settings);
	      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false)).with(HINGE, DoorHinge.LEFT)).with(POWERED, false)).with(HALF, DoubleBlockHalf.LOWER));
	   }

	   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
	      Direction direction = (Direction)state.get(FACING);
	      boolean bl = !(Boolean)state.get(OPEN);
	      boolean bl2 = state.get(HINGE) == DoorHinge.RIGHT;
	      switch(direction) {
	      case EAST:
	      default:
	         return bl ? WEST_SHAPE : (bl2 ? SOUTH_SHAPE : NORTH_SHAPE);
	      case SOUTH:
	         return bl ? NORTH_SHAPE : (bl2 ? WEST_SHAPE : EAST_SHAPE);
	      case WEST:
	         return bl ? EAST_SHAPE : (bl2 ? NORTH_SHAPE : SOUTH_SHAPE);
	      case NORTH:
	         return bl ? SOUTH_SHAPE : (bl2 ? EAST_SHAPE : WEST_SHAPE);
	      }
	   }

	   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, World world, BlockPos pos, BlockPos neighborPos) {
	      DoubleBlockHalf doubleBlockHalf = (DoubleBlockHalf)state.get(HALF);
	      if (facing.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
	         return neighborState.getBlock() == this && neighborState.get(HALF) != doubleBlockHalf ? (BlockState)((BlockState)((BlockState)((BlockState)state.with(FACING, neighborState.get(FACING))).with(OPEN, neighborState.get(OPEN))).with(HINGE, neighborState.get(HINGE))).with(POWERED, neighborState.get(POWERED)) : Blocks.AIR.getDefaultState();
	      } else {
	         return doubleBlockHalf == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
	      }
	   }

	   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
	      super.afterBreak(world, player, pos, Blocks.AIR.getDefaultState(), blockEntity, stack);
	   }

	   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
	      DoubleBlockHalf doubleBlockHalf = (DoubleBlockHalf)state.get(HALF);
	      BlockPos blockPos = doubleBlockHalf == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
	      BlockState blockState = world.getBlockState(blockPos);
	      if (blockState.getBlock() == this && blockState.get(HALF) != doubleBlockHalf) {
	         world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 35);
	         world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
	         ItemStack itemStack = player.getMainHandStack();
	         if (!world.isClient && !player.isCreative() && player.isUsingEffectiveTool(blockState)) {
	            Block.dropStacks(state, world, pos, (BlockEntity)null, player, itemStack);
	            Block.dropStacks(blockState, world, blockPos, (BlockEntity)null, player, itemStack);
	         }
	      }

	      super.onBreak(world, pos, state, player);
	   }

	   public boolean canPlaceAtSide(BlockState state, BlockView world, BlockPos pos, NavigationType env) {
	      switch(env) {
	      case LAND:
	         return (Boolean)state.get(OPEN);
	      case WATER:
	         return false;
	      case AIR:
	         return (Boolean)state.get(OPEN);
	      default:
	         return false;
	      }
	   }

	   private int getOpenSoundEventId() {
	      return this.material == Material.METAL ? 1011 : 1012;
	   }

	   private int getCloseSoundEventId() {
	      return this.material == Material.METAL ? 1005 : 1006;
	   }

	   public BlockState getPlacementState(ItemPlacementContext ctx) {
	      BlockPos blockPos = ctx.getBlockPos();
	      if (blockPos.getY() < 255 && ctx.getWorld().getBlockState(blockPos.up()).canReplace(ctx)) {
	         World world = ctx.getWorld();
	         boolean bl = world.isReceivingRedstonePower(blockPos) || world.isReceivingRedstonePower(blockPos.up());
	         return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing())).with(HINGE, this.getHinge(ctx))).with(POWERED, bl)).with(OPEN, bl)).with(HALF, DoubleBlockHalf.LOWER);
	      } else {
	         return null;
	      }
	   }

	   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
	      world.setBlockState(pos.up(), (BlockState)state.with(HALF, DoubleBlockHalf.UPPER), 3);
	   }

	   private DoorHinge getHinge(ItemPlacementContext ctx) {
	      BlockView blockView = ctx.getWorld();
	      BlockPos blockPos = ctx.getBlockPos();
	      Direction direction = ctx.getPlayerFacing();
	      BlockPos blockPos2 = blockPos.up();
	      Direction direction2 = direction.rotateYCounterclockwise();
	      BlockPos blockPos3 = blockPos.offset(direction2);
	      BlockState blockState = blockView.getBlockState(blockPos3);
	      BlockPos blockPos4 = blockPos2.offset(direction2);
	      BlockState blockState2 = blockView.getBlockState(blockPos4);
	      Direction direction3 = direction.rotateYClockwise();
	      BlockPos blockPos5 = blockPos.offset(direction3);
	      BlockState blockState3 = blockView.getBlockState(blockPos5);
	      BlockPos blockPos6 = blockPos2.offset(direction3);
	      BlockState blockState4 = blockView.getBlockState(blockPos6);
	      int i = (blockState.isFullCube(blockView, blockPos3) ? -1 : 0) + (blockState2.isFullCube(blockView, blockPos4) ? -1 : 0) + (blockState3.isFullCube(blockView, blockPos5) ? 1 : 0) + (blockState4.isFullCube(blockView, blockPos6) ? 1 : 0);
	      boolean bl = blockState.getBlock() == this && blockState.get(HALF) == DoubleBlockHalf.LOWER;
	      boolean bl2 = blockState3.getBlock() == this && blockState3.get(HALF) == DoubleBlockHalf.LOWER;
	      if ((!bl || bl2) && i <= 0) {
	         if ((!bl2 || bl) && i >= 0) {
	            int j = direction.getOffsetX();
	            int k = direction.getOffsetZ();
	            Vec3d vec3d = ctx.getHitPos();
	            double d = vec3d.x - (double)blockPos.getX();
	            double e = vec3d.z - (double)blockPos.getZ();
	            return (j >= 0 || e >= 0.5D) && (j <= 0 || e <= 0.5D) && (k >= 0 || d <= 0.5D) && (k <= 0 || d >= 0.5D) ? DoorHinge.LEFT : DoorHinge.RIGHT;
	         } else {
	            return DoorHinge.LEFT;
	         }
	      } else {
	         return DoorHinge.RIGHT;
	      }
	   }

	   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
	      if (this.material == Material.METAL) {
	         return ActionResult.PASS;
	      } else {
	         state = (BlockState)state.cycle(OPEN);
	         world.setBlockState(pos, state, 10);
	         world.syncWorldEvent(player, (Boolean)state.get(OPEN) ? this.getCloseSoundEventId() : this.getOpenSoundEventId(), pos, 0);
	         return ActionResult.SUCCESS;
	      }
	   }

	   public void setOpen(World world, BlockPos pos, boolean open) {
	      BlockState blockState = world.getBlockState(pos);
	      if (blockState.getBlock() == this && (Boolean)blockState.get(OPEN) != open) {
	         world.setBlockState(pos, (BlockState)blockState.with(OPEN, open), 10);
	         this.playOpenCloseSound(world, pos, open);
	      }
	   }

	   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
	      boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
	      if (block != this && bl != (Boolean)state.get(POWERED)) {
	         if (bl != (Boolean)state.get(OPEN)) {
	            this.playOpenCloseSound(world, pos, bl);
	         }

	         world.setBlockState(pos, (BlockState)((BlockState)state.with(POWERED, bl)).with(OPEN, bl), 2);
	      }

	   }

	   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
	      BlockPos blockPos = pos.down();
	      BlockState blockState = world.getBlockState(blockPos);
	      if (state.get(HALF) == DoubleBlockHalf.LOWER) {
	         return blockState.isSideSolidFullSquare(world, blockPos, Direction.UP);
	      } else {
	         return blockState.getBlock() == this;
	      }
	   }

	   private void playOpenCloseSound(World world, BlockPos pos, boolean open) {
	      world.syncWorldEvent((PlayerEntity)null, open ? this.getCloseSoundEventId() : this.getOpenSoundEventId(), pos, 0);
	   }

	   public PistonBehavior getPistonBehavior(BlockState state) {
	      return PistonBehavior.DESTROY;
	   }

	   public BlockState rotate(BlockState state, BlockRotation rotation) {
	      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
	   }

	   public BlockState mirror(BlockState state, BlockMirror mirror) {
	      return mirror == BlockMirror.NONE ? state : (BlockState)state.rotate(mirror.getRotation((Direction)state.get(FACING))).cycle(HINGE);
	   }

	   @Environment(EnvType.CLIENT)
	   public long getRenderingSeed(BlockState state, BlockPos pos) {
	      return MathHelper.hashCode(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
	   }

	   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
	      builder.add(HALF, FACING, OPEN, HINGE, POWERED);
	   }

	   public static boolean method_24795(World world, BlockPos blockPos) {
	      return method_24796(world.getBlockState(blockPos));
	   }

	   public static boolean method_24796(BlockState blockState) {
	      return blockState.getBlock() instanceof DoorBlock && (blockState.getMaterial() == Material.WOOD || blockState.getMaterial() == Material.NETHER_WOOD);
	   }

	   static {
	      FACING = HorizontalFacingBlock.FACING;
	      OPEN = Properties.OPEN;
	      HINGE = Properties.DOOR_HINGE;
	      POWERED = Properties.POWERED;
	      HALF = Properties.DOUBLE_BLOCK_HALF;
	      NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
	      SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
	      EAST_SHAPE = Block.createCuboidShape(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	      WEST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
	   }
	}
