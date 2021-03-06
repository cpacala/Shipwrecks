package winslow.shipwrecks;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Random;

public class ShipwreckGen implements IWorldGenerator {

    private ShipwreckLoot loot = new ShipwreckLoot();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                         IChunkProvider chunkProvider) {

        switch (world.provider.getDimension()) {
            case 0:
                generateSurface(world, chunkX * 16 + 8, chunkZ * 16 + 8); //Overworld Generation
        }
    }

    /*
     * Generate structures on the surface
     */
    private void generateSurface(World world, int chunkX, int chunkZ) {
        //Get the highest non-air block
        BlockPos pos = new BlockPos(chunkX, 0, chunkZ);//getWorldHeight(world, chunkX, chunkZ);

        int max = ShipwreckConfig.getMaxDist();
        if (canSpawnHere(pos, max)) {
            Random random = new Random();

            Biome bio = world.getBiome(pos);
            String biomeName = bio.getBiomeName().toLowerCase();

            //Random offset from chunk between min and max from center of chunk
            int min = ShipwreckConfig.getMinDist();

            double maxOffset = ((double) max - (double) min) / 2.0;
            int newX = (int) ((random.nextDouble() * maxOffset - 2 * random.nextDouble() * maxOffset) * 16);
            int newZ = (int) ((random.nextDouble() * maxOffset - 2 * random.nextDouble() * maxOffset) * 16);
            pos = pos.add(newX, world.getSeaLevel(), newZ);

            pos = pos.add(0, findSeafloor(world, pos) - pos.getY(), 0);

            if (biomeName.contains("ocean")) //check to generate ship in ocean
                generateStructures(world, getStructureName(true, random), pos);
            else if (biomeName.contains("beach")) //check to generate ship on beach
                generateStructures(world, getStructureName(false, random), pos);
        }
    }

    /*
     * Calls a generate function for a random cardinal direction
     */
    private void generateStructures(World world, String structure, BlockPos pos) {
        if (structure == null)
            return;

        JsonParser parser = new JsonParser();

        try {
            //Read JSON string from structure file
            URL path = ShipwrecksMain.class.getResource("/assets/" + ShipwrecksMain.MODID
                    + "/structures/" + structure + ".json");
            if (path == null) {
                File structurePath;
                try {
                    structurePath = new File("./structures/" + structure + ".json");
                    if(!structurePath.exists())
                        return;
                    path = structurePath.toURI().toURL();
                } catch (Exception ex) {
                    return;
                }
            }
            String textFile = Resources.toString(path, Charsets.UTF_8);

            JsonObject jsonObj = (JsonObject) parser.parse(textFile);

            Random random = new Random();
            int orientation = random.nextInt(4); //E, W, N, S orientation

            // 1 in 6 chance of ship floating
            if (jsonObj.getAsJsonPrimitive("can_float").getAsBoolean() && random.nextInt(6) == 0)
                pos = pos.add(0, world.getSeaLevel() - pos.getY(), 0);

            if (jsonObj.has("sections")) {
                JsonArray sections = jsonObj.getAsJsonArray("sections"); //get sections (an array of objects containing block types and coordinates)

                for (int i = 0; i < sections.size(); ++i) //loop through array and add each segment
                    addBlocksJson(world, sections.get(i).getAsJsonObject(), pos, orientation);
            }
            if (jsonObj.has("random")) //structure pieces that can appear a random orientation and distance from the center of the structure
            {
                JsonArray sections = jsonObj.getAsJsonArray("random");

                for (int i = 0; i < sections.size(); ++i) //loop through array and add each segment
                {
                    JsonObject data = sections.get(i).getAsJsonObject();

                    if (data.has("range")) {
                        JsonArray range = data.getAsJsonArray("range");

                        int min = range.get(0).getAsInt();
                        int max = range.get(1).getAsInt();

                        int xOffset = min + random.nextInt(max - min);
                        int zOffset = min + random.nextInt(max - min);

                        //50% chance to be negative x or y from center of wreck
                        if (random.nextInt(2) == 0)
                            xOffset *= -1;
                        if (random.nextInt(2) == 0)
                            zOffset *= -1;

                        //find new position to act as (0, 0, 0) for random object
                        BlockPos newPos = new BlockPos(pos.getX() + xOffset, pos.getY(), pos.getZ() + zOffset);

                        addBlocksJson(world, data, newPos, orientation);
                    }
                }
            }
            if (jsonObj.has("chance_sections")) //sections that have a given chance to spawn
            {
                JsonArray chance_sections = jsonObj.getAsJsonArray("chance_sections");
                for (int i = 0; i < chance_sections.size(); ++i) {
                    JsonObject data = chance_sections.get(i).getAsJsonObject();
                    Boolean isExclusive = false;
                    if (data.has("exclusive"))
                        isExclusive = data.get("exclusive").getAsBoolean();

                    if (!data.has("chance")) //these sections require a weight field
                        return;
                    JsonArray chance = data.getAsJsonArray("chance");
                    JsonArray sections = data.getAsJsonArray("chance_blocks"); //get sections (an array of objects containing block types and coordinates)

                    for (int j = 0; j < sections.size() && j < chance.size(); ++j) {
                        if (random.nextInt(chance.get(j).getAsInt()) == 0) {
                            JsonArray coords = sections.get(j).getAsJsonArray();
                            for (int k = 0; k < coords.size(); ++k)
                                addBlocksJson(world, coords.get(k).getAsJsonObject(), pos, orientation);

                            if (isExclusive)
                                break;
                        }
                    }
                }
            }
            if (jsonObj.has("damage_sections")) //create damage on ship. Replace removed blocks with block type 1 away from center and 1 Y coord up
            {
                JsonArray damageSections = jsonObj.getAsJsonArray("damage_sections");

                for (int i = 0; i < damageSections.size(); ++i) {
                    JsonObject piece = damageSections.get(i).getAsJsonObject();
                    if (!piece.has("chance")) //these sections require a weight field
                        return;
                    JsonArray chance = piece.getAsJsonArray("chance");
                    JsonArray sections = piece.getAsJsonArray("chance_blocks");

                    for (int j = 0; j < chance.size() && j < sections.size(); ++j) {
                        if (random.nextInt(chance.get(j).getAsInt()) == 0) {
                            JsonObject data = sections.get(j).getAsJsonObject(); //get block coordinates
                            JsonArray coordArray = data.getAsJsonArray("coords");

                            for (int k = 0; k < coordArray.size(); ++k) {
                                JsonArray coords = coordArray.get(k).getAsJsonArray();
                                int index = 0;
                                int x = coords.get(index).getAsInt();
                                ++index;
                                int y = coords.get(index).getAsInt() - 1;
                                ++index;
                                int z = coords.get(index).getAsInt();
                                ++index;

                                //convert coords to correct position based on orientation
                                switch (orientation) {
                                    case 1: //West
                                        x = -x;
                                        z = -z;
                                        break;
                                    case 2: //North
                                        int tempN = x;
                                        x = -z;
                                        z = tempN;
                                        break;
                                    case 3: //South
                                        int tempS = -x;
                                        x = z;
                                        z = tempS;
                                        break;
                                }

                                //facing determines which side of the block to get the replacement block from
                                BlockPos blkSource = pos.add(x, y, z);
                                EnumFacing dir = getFacing(orientation, piece.get("facing").getAsString());
                                blkSource = blkSource.offset(dir);
                                blkSource = blkSource.up();

                                world.setBlockState(pos.add(x, y, z), world.getBlockState(blkSource));
                            }
                        }
                    }
                }
            }
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Check if wreck can spawn here based on min and max distances set in config
     */
    private Boolean canSpawnHere(BlockPos pos, int maxDist) {
        int xVal = (pos.getX() / 16) % maxDist;
        int zVal = (pos.getZ() / 16) % maxDist;
        //wrecks can spawn only on (maxDist, Y, maxDist) nodes) but get offset a random distance from there
        return (xVal == 0 && zVal == 0);
    }

    /*
     * adds blocks to the world with positions read from passed JsonObject
     */
    @SuppressWarnings("unchecked") //suppressed as cast exceptions will be caught and I haven't found a better solution for casting properties
    private void addBlocksJson(World world, JsonObject jsonObj, BlockPos pos, int orientation) {
        if (!jsonObj.has("block") || !jsonObj.has("coords")) //missing required field, don't know what block to add/where to put them
            return;

        //get block type to add
        String blockType = jsonObj.get("block").getAsString();
        Block block = Block.getBlockFromName(blockType);

        if (block == null) //blockType incorrect, unknown block to add.
            return;

        IBlockState blkState = block.getDefaultState();
        Collection<IProperty<?>> propertyKeys = block.getDefaultState().getPropertyKeys();
        String value;

        //iterate over the block's properties and add any values that appear in the json.
        try {
            for (IProperty<?> property : propertyKeys) {
                if (property.getName().equals("facing") && jsonObj.has("facing")) {
                    value = jsonObj.get("facing").getAsString();
                    blkState = blkState.withProperty((PropertyDirection) property, getFacing(orientation, value));
                } else if (property.getName().equals("axis") && jsonObj.has("axis")) {
                    value = jsonObj.get("axis").getAsString();
                    blkState = blkState.withProperty((PropertyEnum) property, getAxis(orientation, value));
                } else if (property.getName().equals("variant") && jsonObj.has("variant")) {
                    value = jsonObj.get("variant").getAsString();
                    blkState = blkState.withProperty((PropertyEnum) property, EnumType.valueOf(value));
                } else if (property.getName().equals("half") && jsonObj.has("half")) {
                    value = jsonObj.get("half").getAsString();
                    if (block.getUnlocalizedName().contains("door"))
                        blkState = blkState.withProperty((PropertyEnum) property, BlockDoor.EnumDoorHalf.valueOf(value));
                    else if (block.getUnlocalizedName().contains("stair"))
                        blkState = blkState.withProperty((PropertyEnum) property, BlockStairs.EnumHalf.valueOf(value));
                    else
                        blkState = blkState.withProperty((PropertyEnum) property, EnumBlockHalf.valueOf(value));
                } else if (property.getName().equals("part") && jsonObj.has("part")) {
                    value = jsonObj.get("part").getAsString();
                    blkState = blkState.withProperty((PropertyEnum) property, EnumPartType.valueOf(value));
                }
            }
        }
        catch (ClassCastException e) {
            System.out.println(e.getMessage());
        }

        JsonArray coords = jsonObj.getAsJsonArray("coords"); //array of block positions. "coords" existence checked at beginning of function

        for (int i = 0; i < coords.size(); ++i) {
            JsonArray posArray = coords.get(i).getAsJsonArray(); //get first set of coords

            int index = 0;
            int x = posArray.get(index).getAsInt();
            ++index;
            int y = posArray.get(index).getAsInt() - 1;
            ++index;
            int z = posArray.get(index).getAsInt();
            ++index;

            //convert coords to correct position based on orientation
            switch (orientation) {
                case 1: //West
                    x = -x;
                    z = -z;
                    break;
                case 2: //North
                    int tempN = x;
                    x = -z;
                    z = tempN;
                    break;
                case 3: //South
                    int tempS = -x;
                    x = z;
                    z = tempS;
                    break;
            }

            world.setBlockState(pos.add(x, y, z), blkState);

            if (jsonObj.has("loot")) //process blocks with inventory differently (e.g. chests have loot tiers)
            {
                String lootPool = jsonObj.get("loot").getAsString();
                loot.addChestLoot(world, pos.add(x, y, z), lootPool);
            }
        }
    }

    /*
     * Get the facing direction and rotate the face of the object from the default East facing to the
     * correct facing for W, N, or S facing structures
     */
    private EnumFacing getFacing(int orientation, String facing) {

        EnumFacing dir = EnumFacing.byName(facing);//EnumFacing.EAST;

        if (dir != null) {
            switch (orientation) {
                case 1:    //West
                    dir = dir.rotateY();
                    dir = dir.rotateY();
                    break;
                case 2:    //North
                    dir = dir.rotateY();
                    break;
                case 3:    //South
                    dir = dir.rotateYCCW();
                    break;
            }
        }
        return dir;
    }

    /*
     * Get the axis direction and rotate the object from the default East facing to the
     * correct facing for W, N, or S facing structures
     */
    private EnumAxis getAxis(int orientation, String facing) {
        EnumAxis axis = EnumAxis.valueOf(facing);//EnumAxis.Y;
        if (axis == EnumAxis.Y)
            return axis;

        if (axis == EnumAxis.NONE)
            return axis;

        if (orientation == 2 || orientation == 3)
            return (axis == EnumAxis.X) ? EnumAxis.Z : EnumAxis.X;

        return axis;
    }

    /*
     * Finds the highest non-water/non-air block at the passed x and z coordinates
     * Returns the y coordinate for the found height and x and z coords
     */
    private int findSeafloor(World world, BlockPos pos) {
        //start at the highest block
        while (world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.AIR)
            pos = pos.down();

        return pos.getY();
    }

    /*
     * Get the correct name for the structure to generate based on the corresponding weight
     *
     * parameters: isOceanBiome, true = is an ocean biome, false = is not (it's a beach biome)
     */
    private String getStructureName(Boolean isOceanBiome, Random random) {
        int[] weights;
        //get the correct weights for structures based on the biome
        if (isOceanBiome)
            weights = ShipwreckConfig.getOceanWeights();
        else
            weights = ShipwreckConfig.getBeachWeights();

        //find the sum of all weights
        int totalWeight = 0;
        for (int weight : weights)
            totalWeight += weight;

        int value = random.nextInt(totalWeight);
        totalWeight = 0;
        for (int i = 0; i < weights.length; ++i) {
            totalWeight += weights[i];
            if (totalWeight >= value) {
//                if (i == 0) //the first index, weighted value for no ships to spawn so don't return a name
//                    return null;
//                else //return the name of the wreck corresponding to the weight
                return ShipwreckConfig.getNames()[i]; //i - 1 as the weighted array has the no spawn value at index 0
            }
        }
        return null;
    }
}
