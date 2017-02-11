package com.winslow.shipwrecks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class ShipwreckGen implements IWorldGenerator{

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider) {
		
		switch(world.provider.getDimension())
		{
			case 0: generateSurface(world, chunkX * 16, chunkZ * 16); //Overworld Generation
		}
	}
	
	/*
	 * Generate structures on the surface
	 */
	private void generateSurface(World world, int chunkX, int chunkZ)
	{
		//Get the highest non-air block
		BlockPos pos = new BlockPos(chunkX, world.getHeight(chunkX, chunkZ), chunkZ);//getWorldHeight(world, chunkX, chunkZ);
		Random random = new Random();
		
		Biome bio = world.getBiome(pos);
		String biomeName = bio.getBiomeName().toLowerCase();
		
		String structure = "";
		
		//choose the structure to generate, WILL BE REPLACED WITH WEIGHTED CHOICE BASED ON USER INPUT
		switch(random.nextInt(2))
		{
			case 0:
				structure = "rowboat";
				break;
				
			case 1:
				structure = "sailboatup";
				break;
		}
		
		if(biomeName.contains("ocean")) //check to generate ship in ocean
			generateStructures(world, structure, pos);
		else if(biomeName.contains("beach")) //check to generate ship on beach
			generateStructures(world, structure, pos);
	}
	
	/*
	 * Calls a generate function for a random cardinal direction
	 */
	private void generateStructures(World world, String structure, BlockPos pos)
	{
		Random random = new Random();
		switch(random.nextInt(4)) //Spawn ship facing random direction
		{
			case 0:
				generateStructureEast(world, structure, pos);
				break;
			case 1:
				generateStructureWest(world, structure, pos);
				break;
			case 2:
				generateStructureNorth(world, structure, pos);
				break;
			case 3:
				generateStructureSouth(world, structure, pos);
				break;
		}
	}
	
	/*
	 * Finds the highest non-air block at the passed x and z coordinates
	 * Returns the BlockPos for the found height and x and z coords
	 */
	protected BlockPos getWorldHeight(World world, int x, int z)
	{
		//start at the highest block
		BlockPos pos = new BlockPos(x, world.getHeight(x, z), z);
		for(int i = pos.getY(); i > 63; --i)
		{
			pos = pos.down();
			if(world.getBlockState(pos).getBlock() != Blocks.AIR)
				return pos;
		}
		return pos;
	}

	/*
	 * Generate structure facing East.
	 */
	private void generateStructureEast(World world, String structure, BlockPos pos)
	{
		JsonParser parser = new JsonParser();
		
		try
		{
			//Read JSON string from structure file
			String textFile = Resources.toString(ShipwrecksMain.class.getResource(
					"/assets/" + ShipwrecksMain.MODID + "/structures/" + structure + ".json"), Charsets.UTF_8);
			
			JsonObject jsonObj = (JsonObject) parser.parse(textFile);
			addBlocksJsonEast(world, jsonObj, pos, "hull", Blocks.PLANKS); //add ship hull to the world
			addBlocksJsonEast(world, jsonObj, pos, "mast", Blocks.LOG); //add ship mast to the world
			addBlocksJsonEast(world, jsonObj, pos, "chest", Blocks.CHEST); //add ship mast to the world

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Generate structure facing West.
	 */
	private void generateStructureWest(World world, String structure, BlockPos pos)
	{
		JsonParser parser = new JsonParser();
		
		try
		{
			//Read JSON string from structure file
			String textFile = Resources.toString(ShipwrecksMain.class.getResource(
					"/assets/" + ShipwrecksMain.MODID + "/structures/" + structure + ".json"), Charsets.UTF_8);
			
			JsonObject jsonObj = (JsonObject) parser.parse(textFile);
			addBlocksJsonWest(world, jsonObj, pos, "hull", Blocks.PLANKS); //add ship hull to the world
			addBlocksJsonWest(world, jsonObj, pos, "mast", Blocks.LOG); //add ship mast to the world
			addBlocksJsonWest(world, jsonObj, pos, "chest", Blocks.CHEST); //add ship mast to the world

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Generate structure facing North.
	 */
	private void generateStructureNorth(World world, String structure, BlockPos pos)
	{
		JsonParser parser = new JsonParser();
		
		try
		{
			//Read JSON string from structure file
			String textFile = Resources.toString(ShipwrecksMain.class.getResource(
					"/assets/" + ShipwrecksMain.MODID + "/structures/" + structure + ".json"), Charsets.UTF_8);
			
			JsonObject jsonObj = (JsonObject) parser.parse(textFile);
			addBlocksJsonNorth(world, jsonObj, pos, "hull", Blocks.PLANKS); //add ship hull to the world
			addBlocksJsonNorth(world, jsonObj, pos, "mast", Blocks.LOG); //add ship mast to the world
			addBlocksJsonNorth(world, jsonObj, pos, "chest", Blocks.CHEST); //add ship mast to the world

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Generate structure facing South.
	 */
	private void generateStructureSouth(World world, String structure, BlockPos pos)
	{
		JsonParser parser = new JsonParser();
		
		try
		{
			//Read JSON string from structure file
			String textFile = Resources.toString(ShipwrecksMain.class.getResource(
					"/assets/" + ShipwrecksMain.MODID + "/structures/" + structure + ".json"), Charsets.UTF_8);
			
			JsonObject jsonObj = (JsonObject) parser.parse(textFile);
			addBlocksJsonSouth(world, jsonObj, pos, "hull", Blocks.PLANKS); //add ship hull to the world
			addBlocksJsonSouth(world, jsonObj, pos, "mast", Blocks.LOG); //add ship mast to the world
			addBlocksJsonSouth(world, jsonObj, pos, "chest", Blocks.CHEST); //add ship mast to the world

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * read blocks from the passed jsonObj and the array with the key stored in "structurPiece".
	 */
	private void addBlocksJsonEast(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block)
	{
		JsonArray blocks;
		
		if(jsonObj.has(structurePiece))
		{
			blocks = jsonObj.getAsJsonArray(structurePiece);
			if(blocks != null)
			{
				for (int i = 0; i < blocks.size(); ++i)
				{
					JsonArray posArray = (JsonArray) blocks.get(i);
					if(posArray.size() == 3) //blocks without metadata (only position data)
					{
						int x = posArray.get(0).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(2).getAsInt();
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
					}
					else if(posArray.size() == 4) //blocks with metadata
					{
						int x = posArray.get(0).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(2).getAsInt();
						int md = posArray.get(3).getAsInt();
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
					}
				}
			}
		}
	}
	
	/*
	 * read blocks from the passed jsonObj and the array with the key stored in "structurPiece".
	 */
	private void addBlocksJsonWest(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block)
	{
		JsonArray blocks;
		
		if(jsonObj.has(structurePiece))
		{
			blocks = jsonObj.getAsJsonArray(structurePiece);
			if(blocks != null)
			{
				for (int i = 0; i < blocks.size(); ++i)
				{
					JsonArray posArray = (JsonArray) blocks.get(i);
					if(posArray.size() == 3) //blocks without metadata (only position data)
					{
						int x = -1 * posArray.get(0).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(2).getAsInt();
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
					}
					else if(posArray.size() == 4) //blocks with metadata
					{
						int x = -1 * posArray.get(0).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(2).getAsInt();
						int md = posArray.get(3).getAsInt();
						
						//convert metadata to face correct direction
						switch(md)
						{
							case 2:
								md = 3;
								break;
							case 3:
								md = 2;
								break;
							case 4:
								md = 5;
								break;
							case 5:
								md = 4;
								break;
						}
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
					}
				}
			}
		}
	}
	
	/*
	 * read blocks from the passed jsonObj and the array with the key stored in "structurPiece".
	 */
	private void addBlocksJsonNorth(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block)
	{
		JsonArray blocks;
		
		if(jsonObj.has(structurePiece))
		{
			blocks = jsonObj.getAsJsonArray(structurePiece);
			if(blocks != null)
			{
				for (int i = 0; i < blocks.size(); ++i)
				{
					JsonArray posArray = (JsonArray) blocks.get(i);
					if(posArray.size() == 3) //blocks without metadata (only position data)
					{
						int x = posArray.get(2).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = -1 * posArray.get(0).getAsInt();
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
					}
					else if(posArray.size() == 4) //blocks with metadata
					{
						int x = posArray.get(2).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = -1 * posArray.get(0).getAsInt();
						int md = posArray.get(3).getAsInt();
						
						//convert metadata to face correct direction
						switch(md)
						{
							case 2:
								md = 4;
								break;
							case 3:
								md = 5;
								break;
							case 4:
								md = 3;
								break;
							case 5:
								md = 2;
								break;
						}
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
					}
				}
			}
		}
	}
	
	/*
	 * read blocks from the passed jsonObj and the array with the key stored in "structurPiece".
	 */
	private void addBlocksJsonSouth(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block)
	{
		JsonArray blocks;
		
		if(jsonObj.has(structurePiece))
		{
			blocks = jsonObj.getAsJsonArray(structurePiece);
			if(blocks != null)
			{
				for (int i = 0; i < blocks.size(); ++i)
				{
					JsonArray posArray = (JsonArray) blocks.get(i);
					if(posArray.size() == 3) //blocks without metadata (only position data)
					{
						int x = posArray.get(2).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(0).getAsInt();
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
					}
					else if(posArray.size() == 4) //blocks with metadata
					{
						int x = posArray.get(2).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(0).getAsInt();
						int md = posArray.get(3).getAsInt();
						
						//convert metadata to face correct direction
						switch(md)
						{
							case 2:
								md = 5;
								break;
							case 3:
								md = 4;
								break;
							case 4:
								md = 2;
								break;
							case 5:
								md = 3;
								break;
						}
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
					}
				}
			}
		}
	}
	
	/*
	 * Add a block without metadata
	 */
	private void addBlock(World world, int x, int y, int z, Block block)
	{
		world.setBlockState(new BlockPos(x, y, z), block.getDefaultState());
	}
	
	/*
	 * Add a block with metadata
	 */
	@SuppressWarnings("deprecation") //suppressed as getStateFromMeta is not actually deprecated by Mojang
	private void addBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(metadata)); //getStateFromMeta not actually deprecated
	}
}
