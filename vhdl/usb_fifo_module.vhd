LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
USE IEEE.STD_LOGIC_ARITH.ALL;
USE IEEE.STD_LOGIC_UNSIGNED."+";
USE IEEE.NUMERIC_STD.ALL;

ENTITY usb_fifo_module IS
    
    GENERIC
    (
        USB_FIFO_DATA_WIDTH : INTEGER := 7
    );
    
    PORT 
    ( 
        clk                 : IN STD_LOGIC;
        reset               : IN STD_LOGIC;
        fifo_data_available : IN STD_LOGIC;
        fifo_data           : IN STD_LOGIC_VECTOR( USB_FIFO_DATA_WIDTH DOWNTO 0 );
        
        packet_contents     : OUT STD_LOGIC_VECTOR( USB_FIFO_DATA_WIDTH DOWNTO 0 );
        new_data_packet     : OUT STD_LOGIC; -- Held for one clock cycle after 
                                            -- valid data has been read
        fifo_reading_data   : OUT STD_LOGIC  -- Held for 100ns minimum
    );
END usb_fifo_module;

ARCHITECTURE usb_fifo_module_arch OF usb_fifo_module IS

-- FIFO reading state machine
TYPE FIFO_READING_MODES IS ( FIFO_RESET, FIFO_WAIT_FOR_DATA, FIFO_START_READING, FIFO_READ_DATA, 
                             FIFO_END_NEW_DATA, FIFO_END_READ );

SIGNAL current_state, 
       next_state    : FIFO_READING_MODES;

-- Clock divider for READ timing
CONSTANT CLK_DIVIDER_RESET_VALUE : STD_LOGIC_VECTOR( 2 DOWNTO 0 ) := "000";

CONSTANT NB_OF_CLOCK_CYCLES_FOR_ENABLE : STD_LOGIC_VECTOR( 2 DOWNTO 0 ) := "100";

SIGNAL enable_clk_divider   : STD_LOGIC;

SIGNAL read_clock_enable    : STD_LOGIC;
SIGNAL clock_enable_counter : STD_LOGIC_VECTOR( 2 DOWNTO 0 );

BEGIN
    
    -- Create a clk/5 clock enable signal to respect the USB FIFO sequence timing
    PROCESS( reset, current_state ) IS
    BEGIN
        IF reset = '0' AND 
           ( current_state = FIFO_START_READING OR 
             current_state = FIFO_READ_DATA OR
             current_state = FIFO_END_NEW_DATA ) THEN
             
            enable_clk_divider <= '1';
            
        ELSE
        
            enable_clk_divider <= '0';
            
        END IF;
    END PROCESS;
    
    -- Divide the clock to generate the clock enable signal
    PROCESS( clk, enable_clk_divider ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
            IF enable_clk_divider = '0' OR 
               clock_enable_counter = NB_OF_CLOCK_CYCLES_FOR_ENABLE THEN
               
                clock_enable_counter <= CLK_DIVIDER_RESET_VALUE;
                
            ELSE
            
                clock_enable_counter <= clock_enable_counter + 1;
                
            END IF;
        END IF;
    END PROCESS;
    
    
    WITH clock_enable_counter SELECT
        read_clock_enable <= 
            '1' WHEN NB_OF_CLOCK_CYCLES_FOR_ENABLE, 
            '0' WHEN OTHERS;
    
    -- Manage the current reading state
    PROCESS( clk, reset ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
            IF reset = '1' THEN 
                current_state <= FIFO_RESET;
            ELSE
                current_state <= next_state;
            END IF;
        END IF;
    END PROCESS;
    
    -- Manage the next reading state
    PROCESS( reset, current_state, fifo_data_available, read_clock_enable ) IS
    BEGIN 
        IF reset = '1' THEN 
        
            next_state <= FIFO_RESET;
            
        ELSIF ( current_state = FIFO_RESET OR current_state = FIFO_END_READ ) AND 
              fifo_data_available = '0'  THEN 
              
            next_state <= FIFO_WAIT_FOR_DATA;
            
        ELSIF current_state = FIFO_WAIT_FOR_DATA AND fifo_data_available = '1' THEN
        
            next_state <= FIFO_START_READING;
            
        ELSIF current_state = FIFO_START_READING AND read_clock_enable = '1' THEN
        
            next_state <= FIFO_READ_DATA;
            
        ELSIF current_state = FIFO_READ_DATA THEN
        
            next_state <= FIFO_END_NEW_DATA;
            
        ELSIF current_state = FIFO_END_NEW_DATA AND read_clock_enable = '1' THEN
        
            next_state <= FIFO_END_READ;
            
        ELSE
        
            next_state <= current_state;
            
        END IF;
    END PROCESS;
    
    -- Manage the FIFO read operation
    PROCESS( clk, current_state ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
            IF current_state = FIFO_RESET THEN
            
                packet_contents <= "00000000";
                new_data_packet <= '0';
                
            ELSIF current_state = FIFO_READ_DATA THEN
            
                packet_contents <= fifo_data;
                new_data_packet <= '1';
                
            ELSE
            
                new_data_packet <= '0';
                
            END IF;
        END IF;
    END PROCESS;
    
    -- Create the FIFO_READING signal (RD#)
    WITH current_state SELECT
        fifo_reading_data <= 
            '1' WHEN FIFO_START_READING, 
            '1' WHEN FIFO_READ_DATA, 
            '1' WHEN FIFO_END_NEW_DATA, 
            '0' WHEN OTHERS;
    
END usb_fifo_module_arch;

