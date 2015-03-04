-- VHDL Test Bench Created by ISE for module: usb_fifo_module
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.std_logic_unsigned.all;
 
ENTITY uut_usb_fifo_module IS
END uut_usb_fifo_module;
 
ARCHITECTURE behavior OF uut_usb_fifo_module IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT usb_fifo_module
    PORT(
        clk                 : IN STD_LOGIC;
        reset               : IN STD_LOGIC;
        fifo_data_available : IN STD_LOGIC;
        fifo_data           : IN STD_LOGIC_VECTOR(  7 DOWNTO 0 );
        packet_contents     : OUT STD_LOGIC_VECTOR( 7 DOWNTO 0 );
        new_data_packet     : OUT STD_LOGIC; 
        fifo_reading_data   : OUT STD_LOGIC
        );
    END COMPONENT;
    

   --Inputs
   signal clk : std_logic := '0';
   signal reset : std_logic := '0';
   signal data_available : std_logic := '0';
   signal data_from_fifo : std_logic_vector(7 downto 0);

 	--Outputs
   signal usb_data : std_logic_vector(7 downto 0);
   signal new_fifo_data : std_logic;
   signal reading_data : std_logic;

   -- Clock period definitions
   constant clk_period : time := 12 ns;
    
   SIGNAL loop_forever : STD_LOGIC := '1';
    
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: usb_fifo_module PORT MAP (
          clk => clk,
          reset => reset,
          fifo_data_available => data_available,
          fifo_data           => data_from_fifo,
          packet_contents => usb_data,
          new_data_packet => new_fifo_data,
          fifo_reading_data => reading_data
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
      -- insert stimulus here 
      reset <= '1';
      
      wait for clk_period;
      
      reset <= '0';
      
      wait for 50 ns;
      
      data_from_fifo <= "10101010";
      
      while loop_forever = '1' loop
      
          data_available <= '1';
          
          wait until reading_data = '1';
          
          wait for 50 ns;
          
          wait until reading_data = '0';
          
          wait for 20 ns;
          
          data_available <= '0';
          data_from_fifo <= NOT data_from_fifo;
          
          wait for 50 ns;
      
      end loop;
      
     
      wait;
   end process;

END;
