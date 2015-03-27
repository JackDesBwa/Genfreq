LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
USE IEEE.STD_LOGIC_ARITH.ALL;
USE IEEE.NUMERIC_STD.ALL;

ENTITY sram_module IS
    PORT( 
        clk           : IN STD_LOGIC;
        reset         : IN STD_LOGIC;
        
        -- SRAM Interface
        data          : INOUT STD_LOGIC_VECTOR( 15 DOWNTO 0 );
        address       : OUT STD_LOGIC_VECTOR( 15 DOWNTO 0 );
        write_enable  : OUT STD_LOGIC;
        chip_enable   : OUT STD_LOGIC;
        output_enable : OUT STD_LOGIC;
        
        -- Abstraction layer
        data_address  : IN STD_LOGIC_VECTOR( 15 DOWNTO 0 );
        data_to_write : IN STD_LOGIC_VECTOR( 0 to 15 );
        write_data    : IN STD_LOGIC;
        data_read     : OUT STD_LOGIC_VECTOR( 0 to 15 );
        read_data     : IN STD_LOGIC
    );
END sram_module;

ARCHITECTURE sram_module_arch OF sram_module IS

CONSTANT DEFAULT_ADDRESS : STD_LOGIC_VECTOR( 15 DOWNTO 0 ) := "0000000000000000";
CONSTANT DEFAULT_DATA    : STD_LOGIC_VECTOR( 15 DOWNTO 0 ) := "0000000000000000";

SIGNAL write_en,  
       out_en   : STD_LOGIC;
       
BEGIN
    
    -- Implement a tri-state input to be able to read/write from the same bus
    data <= data_to_write WHEN ( write_en = '1' ) ELSE ( OTHERS => 'Z' );
    
    data_read <= data;
    
    -- Select the write enable and output enable depending on the current mode
    PROCESS( clk, reset, write_data, read_data ) IS
    BEGIN 
        IF RISING_EDGE( clk ) THEN
            IF reset = '1' THEN
                
                write_en <= '0';
                out_en   <= '0';
                
                address <= DEFAULT_ADDRESS;
                
            ELSIF ( write_data = '0' AND read_data = '0' ) THEN
                
                write_en <= '0';
                out_en   <= '0';
                
            ELSIF write_data = '1' THEN
                write_en <= '1';
                out_en   <= '0';
                
                address <= data_address;
            ELSIF read_data = '1' THEN
            
                write_en <= '0';
                out_en   <= '1';
                
                address <= data_address;
                
            END IF;
        END IF;
    END PROCESS;
    
    -- Perform the hardware abstraction (the SRAM inputs are inverted)
    chip_enable   <= '0';
    
    output_enable <= NOT out_en;
    write_enable  <= NOT write_en;    
    
END sram_module_arch;

