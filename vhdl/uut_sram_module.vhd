-- VHDL Test Bench Created by ISE for module: sram_module
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.std_logic_unsigned.all;
USE ieee.numeric_std.ALL;
 
ENTITY uut_sram_module IS
END uut_sram_module;
 
ARCHITECTURE behavior OF uut_sram_module IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT sram_module
    PORT(
         clk : IN  std_logic;
         reset : IN  std_logic;
         data : INOUT  std_logic_vector(15 downto 0);
         address : OUT  std_logic_vector(15 downto 0);
         write_enable : OUT  std_logic;
         chip_enable : OUT  std_logic;
         output_enable : OUT  std_logic;
         data_address : IN  std_logic_vector(15 downto 0);
         data_to_write : IN  std_logic_vector(15 downto 0);
         write_data : IN  std_logic;
         data_read : OUT  std_logic_vector(15 downto 0);
         read_data : IN  std_logic
        );
    END COMPONENT;
    

   --Inputs
   signal clk : std_logic := '0';
   signal reset : std_logic := '0';
   signal data_address : std_logic_vector(15 downto 0) := (others => '0');
   signal data_to_write : std_logic_vector(15 downto 0) := (others => '0');
   signal write_data : std_logic := '0';
   signal read_data : std_logic := '0';

	--BiDirs
   signal data : std_logic_vector(15 downto 0);

 	--Outputs
   signal address : std_logic_vector(15 downto 0);
   signal write_enable : std_logic;
   signal chip_enable : std_logic;
   signal output_enable : std_logic;
   signal data_read : std_logic_vector(15 downto 0);

   -- Clock period definitions
   constant clk_period : time := 12.5 ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: sram_module PORT MAP (
          clk => clk,
          reset => reset,
          data => data,
          address => address,
          write_enable => write_enable,
          chip_enable => chip_enable,
          output_enable => output_enable,
          data_address => data_address,
          data_to_write => data_to_write,
          write_data => write_data,
          data_read => data_read,
          read_data => read_data
        );

   -- Clock process definitions
   clk_process :process
   begin
		clk <= '0';
		wait for clk_period/2;
		clk <= '1';
		wait for clk_period/2;
   end process;
 

   -- Stimulus process
   stim_proc: process
   begin		
      -- hold reset state for 100ms.
      wait for 1 ms;	
      
      WAIT FOR clk_period / 2;
        
      -- insert stimulus here 
      reset <= '1';
      
      WAIT FOR clk_period;
      
      reset <= '0';
      
      WAIT FOR clk_period;
      
      write_data <= '1';
      data_to_write <= "1010101010101010";
      data_address  <= "0101010101010101";
      
      WAIT FOR clk_period;
      
      write_data <= '0';
      
      WAIT FOR clk_period;
      
      read_data    <= '1';
      data_address <= "0101010101010101";
      
      if( address = "0101010101010101" ) THEN
        data <= "1010101010101010";
      ELSE
        data <= "UUUUUUUUUUUUUUUU";
      END IF;
      
      WAIT FOR clk_period;
      
      write_data <= '1';
      data_to_write <= "0101010101010101";
      data_address  <= "1010101010101010";
      
      wait;
   end process;

END;
