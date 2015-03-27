-- VHDL Test Bench Created by ISE for module: top_level
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;
 
ENTITY uut_top_level IS
END uut_top_level;
 
ARCHITECTURE behavior OF uut_top_level IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT top_level
    PORT(
         clk : IN  std_logic;
         i_reset : IN  std_logic;
         i_usb_rxf : IN  std_logic;
         i_usb_data : IN  std_logic_vector(7 downto 0);
         q_usb_rd : OUT  std_logic;
         iq_mem_data : INOUT  std_logic_vector(15 downto 0);
         q_mem_addr : OUT  std_logic_vector(15 downto 0);
         q_mem_oe : OUT  std_logic;
         q_mem_ce : OUT  std_logic;
         q_mem_we : OUT  std_logic;
         q_dac_clk : OUT  std_logic;
         q_dac_data : OUT  std_logic_vector(13 downto 0);
         q_leds : OUT  std_logic_vector(7 downto 0)
        );
    END COMPONENT;
    

   --Inputs
   signal clk : std_logic := '0';
   signal reset : std_logic := '0';
   signal usb_txe : std_logic := '0';
   signal usb_rxf : std_logic := '0';
   signal usb_data : std_logic_vector(7 downto 0) := (others => '0');

	--BiDirs
   signal mem_data : std_logic_vector(15 downto 0);

 	--Outputs
   signal usb_rd : std_logic;
   signal mem_addr : std_logic_vector(15 downto 0);
   signal mem_oe : std_logic;
   signal mem_ce : std_logic;
   signal mem_we : std_logic;
   signal dac_clk : std_logic;
   signal dac_data : std_logic_vector(13 downto 0);
   signal leds : std_logic_vector(7 downto 0);

   -- Clock period definitions
   constant clk_period : time := 12.5 ns;
   constant dac_clk_period : time := 12.5 ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: top_level PORT MAP (
          clk => clk,
          reset => i_reset,
          usb_rxf => i_usb_rxf,
          usb_data => i_usb_data,
          usb_rd => q_usb_rd,
          mem_data => iq_mem_data,
          mem_addr => q_mem_addr,
          mem_oe => q_mem_oe,
          mem_ce => q_mem_ce,
          mem_we => q_mem_we,
          dac_clk => q_dac_clk,
          dac_data => q_dac_data,
          leds => q_leds
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
      WAIT FOR 1 ms;
      
      usb_rxf <= '1';
      
      reset <= '1';
      
      wait for clk_period;
      
      reset <= '0';
      usb_data <= "01000010";
      
      WAIT FOR clk_period;      
      
      usb_rxf <= '0';
      
      WAIT UNTIL usb_rd = '0';
      
      WAIT FOR 50 ns;
      
      WAIT UNTIL usb_rd = '1';
      
      WAIT FOR 10 ns;
      
      usb_rxf <= '1';
      
      WAIT FOR 50 ns;
      
      usb_data <= "00000000";
      
      WAIT FOR clk_period;      
      
      usb_rxf <= '0';
      
      WAIT UNTIL usb_rd = '0';
      
      WAIT FOR 50 ns;
      
      WAIT UNTIL usb_rd = '1';
      
      WAIT FOR 10 ns;
      
      usb_rxf <= '1';
      
      WAIT FOR 100 ns;

      WAIT;
   end process;

END;
