LIBRARY IEEE;
USE IEEE.STD_LOGIC_1164.ALL;
USE IEEE.STD_LOGIC_ARITH.ALL;
USE IEEE.STD_LOGIC_UNSIGNED.ALL;

ENTITY top_level IS
    PORT( 
           clk   : IN STD_LOGIC;
           i_reset : IN STD_LOGIC;
           
           -- USB FIFO ports
           i_usb_rxf  : IN STD_LOGIC;
           i_usb_data : IN STD_LOGIC_VECTOR( 7 downto 0 );
           q_usb_rd   : OUT STD_LOGIC;
           
           -- RAM ports
           iq_mem_data : INOUT STD_LOGIC_VECTOR( 15 downto 0 );
           q_mem_addr  : OUT STD_LOGIC_VECTOR( 15 downto 0 );
           q_mem_oe    : OUT STD_LOGIC;
           q_mem_ce    : OUT STD_LOGIC;
           q_mem_we    : OUT STD_LOGIC;
           
           -- DAC ports
           q_dac_clk  : OUT STD_LOGIC;
           q_dac_data : OUT STD_LOGIC_VECTOR( 0 to 13 );
           
           -- q_leds ports
           q_leds     : OUT STD_LOGIC_VECTOR( 7 downto 0 ) 
     );
END top_level;

ARCHITECTURE top_level_arch OF top_level IS

-- USB FIFO abstraction layer component
COMPONENT usb_fifo_module IS
    
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
        new_data_packet     : OUT STD_LOGIC;
        fifo_reading_data   : OUT STD_LOGIC
    );
END COMPONENT;


-- SRAM abstraction layer component
COMPONENT sram_module IS
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
END COMPONENT;


-- Global state machine states
TYPE SIGNAL_GENERATOR_MODES IS 
    ( SIG_GEN_RESET, SIG_GEN_WAIT_START_COM, SIG_GEN_START_COM, 
      SIG_GEN_WAIT_RECEIVE_COMMAND, SIG_GEN_RECEIVE_COMMAND,
      SIG_GEN_WAIT_RECEIVE_SPEED_MSB, SIG_GEN_RECEIVE_SPEED_MSB, 
      SIG_GEN_WAIT_RECEIVE_SPEED_LSB, SIG_GEN_RECEIVE_SPEED_LSB, 
      SIG_GEN_WAIT_RECEIVE_ATTENUATION, SIG_GEN_RECEIVE_ATTENUATION,
      SIG_GEN_WAIT_RECEIVE_RAM_MSB, SIG_GEN_RECEIVE_RAM_MSB,
      SIG_GEN_WAIT_RECEIVE_RAM_LSB, SIG_GEN_RECEIVE_RAM_LSB, 
      SIG_GEN_STORE_POINT_IN_RAM );


-- USB commands codes
CONSTANT COMMAND_DEFAULT   : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "11111111";
CONSTANT COMMAND_START_COM : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "01000010";
CONSTANT COMMAND_START_GEN : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "00000000";
CONSTANT COMMAND_STOP_GEN  : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "00000001";
CONSTANT COMMAND_RESET_GEN : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "00000010";
CONSTANT COMMAND_SET_SPEED : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "00000011";
CONSTANT COMMAND_SET_ATTN  : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "00000100";
CONSTANT COMMAND_LOAD_PTS  : STD_LOGIC_VECTOR( 7 DOWNTO 0 ) := "00000101";

-- First memory location in the SRAM
CONSTANT SRAM_FIRST_ADDRESS : STD_LOGIC_VECTOR( 15 DOWNTO 0 ) := "0000000000000000";

-- Last memory location in the SRAM
CONSTANT SRAM_LAST_ADDRESS : STD_LOGIC_VECTOR( 15 DOWNTO 0 ) := "1111111111111111";


CONSTANT NB_OF_POINTS_PER_GROUP : STD_LOGIC_VECTOR( 5 DOWNTO 0 ) := "100000"; -- 32

-- I/O debouncing signals
SIGNAL deb_reset, 
       reset : STD_LOGIC;

-- State machine signals
SIGNAL current_state, 
       next_state    : SIGNAL_GENERATOR_MODES;

-- USB FIFO signals
SIGNAL new_data_packet : STD_LOGIC;

SIGNAL packet_contents : STD_LOGIC_VECTOR( 7 DOWNTO 0 );

SIGNAL fifo_data_available, 
       fifo_reading_data   : STD_LOGIC;

-- DAC signals
SIGNAL run_generator : STD_LOGIC;

SIGNAL attenuation : STD_LOGIC_VECTOR( 7 DOWNTO 0 );

-- Memory signals
SIGNAL read_increment : STD_LOGIC_VECTOR( 15 DOWNTO 0 );

SIGNAL read_address, 
       write_address : STD_LOGIC_VECTOR( 15 DOWNTO 0 );

SIGNAL sram_addr, 
       read_data, 
       write_data : STD_LOGIC_VECTOR( 15 DOWNTO 0 );

SIGNAL sram_read, 
       sram_write : STD_LOGIC;

BEGIN
    
    -- Create an instance of the USB FIFO abstraction layer
    usbfifo : usb_fifo_module 
          PORT MAP
          (
            clk                 => clk,
            reset               => reset,
            fifo_data_available => fifo_data_available,
            fifo_data           => i_usb_data,
            
            packet_contents     => packet_contents,
            new_data_packet     => new_data_packet,
            fifo_reading_data   => fifo_reading_data
          );
    
    -- Create an instance of the SRAM abstraction layer    
    sram : sram_module PORT MAP( 
            clk           => clk,
            reset         => reset,
            
            -- SRAM Interface
            data          => iq_mem_data,
            address       => q_mem_addr,
            write_enable  => q_mem_we,
            chip_enable   => q_mem_ce,
            output_enable => q_mem_oe,
            
            -- Abstraction layer
            data_address  => sram_addr, 
            data_to_write => write_data, 
            write_data    => sram_write,
            data_read     => read_data, 
            read_data     => sram_read
        );
    
    -- Debounce the RESET button (transition detector)
    PROCESS( clk ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
        
            reset     <= deb_reset;
            deb_reset <= i_reset;
            
        END IF;
    END PROCESS;
    
    -- Manage the state machine
    PROCESS( clk, reset ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
            IF reset = '1' THEN
            
                current_state <= SIG_GEN_RESET;
                
            ELSE 
            
                current_state <= next_state;
                
            END IF;        END IF;
    END PROCESS;
    
    -- Find the next state
    PROCESS( clk, reset, current_state, new_data_packet, packet_contents ) IS
        
        -- Contains the number of points loaded since the LOAD command
        VARIABLE points_loaded : STD_LOGIC_VECTOR( 5 DOWNTO 0 );
    
    BEGIN
        IF RISING_EDGE( clk ) THEN
            -- Reset the state machine
            IF reset = '1' OR current_state = SIG_GEN_RESET THEN
                
                run_generator  <= '0';
                attenuation    <= "00000000";
                read_increment <= "0000000000000000";
                
                points_loaded  := "000000";
                
                next_state <= SIG_GEN_WAIT_START_COM;
                
            -- Receive the START_COMMUNICATION packet (0x42)
            ELSIF current_state = SIG_GEN_WAIT_START_COM AND 
                  new_data_packet = '1' THEN
                
                points_loaded := "000000";
                
                next_state <= SIG_GEN_START_COM;
                
            -- Wait for the INSTRUCTION packet
            ELSIF ( current_state = SIG_GEN_START_COM ) AND 
                  ( packet_contents = COMMAND_START_COM ) THEN
                
                next_state <= SIG_GEN_WAIT_RECEIVE_COMMAND;
            
            -- Invalid START_COMMUNICATION packet - restart communication
            ELSIF current_state = SIG_GEN_START_COM AND 
                  NOT ( packet_contents = COMMAND_START_COM ) THEN
                  
                next_state <= SIG_GEN_WAIT_START_COM;
                
            -- Receive the INSTRUCTION packet
            ELSIF current_state = SIG_GEN_WAIT_RECEIVE_COMMAND AND 
                  new_data_packet = '1' THEN
                
                next_state <= SIG_GEN_RECEIVE_COMMAND;
            
            -- Process the RESET_GENERATOR instruction
            ELSIF ( current_state = SIG_GEN_RECEIVE_COMMAND ) AND 
                  ( packet_contents = COMMAND_RESET_GEN ) THEN
                
                next_state <= SIG_GEN_RESET;
            
            -- Process the START_GENERATOR instruction
            ELSIF ( current_state = SIG_GEN_RECEIVE_COMMAND ) AND 
                  ( packet_contents = COMMAND_START_GEN ) THEN
                
                run_generator <= '1';
                next_state    <= SIG_GEN_WAIT_START_COM;
                
            -- Process the STOP_GENERATOR instruction
            ELSIF ( current_state = SIG_GEN_RECEIVE_COMMAND ) AND 
                  ( packet_contents = COMMAND_STOP_GEN ) THEN    
                
                run_generator <= '0';
                next_state    <= SIG_GEN_WAIT_START_COM;
             
            -- Receive the SPEED_INSTRUCTION packet
            ELSIF ( current_state = SIG_GEN_RECEIVE_COMMAND ) AND 
                 ( packet_contents = COMMAND_SET_SPEED ) THEN
            
                next_state <= SIG_GEN_WAIT_RECEIVE_SPEED_MSB;
            
            -- Receive the SPEED_MSB packet
            ELSIF current_state = SIG_GEN_WAIT_RECEIVE_SPEED_MSB AND 
                  new_data_packet = '1' THEN
            
                next_state <= SIG_GEN_RECEIVE_SPEED_MSB;
            
            -- Process the SPEED_MSB packet
            ELSIF current_state = SIG_GEN_RECEIVE_SPEED_MSB THEN
            
                read_increment( 15 DOWNTO 8 ) <= packet_contents;
                
                next_state <= SIG_GEN_WAIT_RECEIVE_SPEED_LSB;
            
            -- Receive the SPEED_LSB packet
            ELSIF current_state = SIG_GEN_WAIT_RECEIVE_SPEED_LSB AND 
                  new_data_packet = '1' THEN
            
                next_state <= SIG_GEN_RECEIVE_SPEED_LSB;
            
            -- Process the SPEED_LSB packet
            ELSIF current_state = SIG_GEN_RECEIVE_SPEED_LSB THEN
            
                read_increment( 7 DOWNTO 0 ) <= packet_contents;
                
                next_state <= SIG_GEN_WAIT_START_COM;
            
            -- Receive the ATTENUATION instruction
            ELSIF ( current_state = SIG_GEN_RECEIVE_COMMAND ) AND 
                 ( packet_contents = COMMAND_SET_ATTN ) THEN
                 
                next_state <= SIG_GEN_WAIT_RECEIVE_ATTENUATION;
            
            -- Receive the ATTENUATION packet
            ELSIF current_state = SIG_GEN_WAIT_RECEIVE_ATTENUATION AND 
                  new_data_packet = '1' THEN
                
                next_state <= SIG_GEN_RECEIVE_ATTENUATION;
            
            -- Process the ATTENUATION packet
            ELSIF current_state = SIG_GEN_RECEIVE_ATTENUATION THEN
                
                attenuation <= packet_contents;
                
                next_state  <= SIG_GEN_WAIT_START_COM;
            
            -- Receive the LOAD_POINTS command
            ELSIF ( current_state = SIG_GEN_RECEIVE_COMMAND ) AND 
                  ( packet_contents = COMMAND_LOAD_PTS ) THEN    
                
                run_generator <= '0';
                
                points_loaded := "000000";
                
                next_state <= SIG_GEN_WAIT_RECEIVE_RAM_MSB;
            
            -- Receive the POINT_MSB packet
            ELSIF current_state = SIG_GEN_WAIT_RECEIVE_RAM_MSB AND 
                  new_data_packet = '1' THEN
            
                next_state <= SIG_GEN_RECEIVE_RAM_MSB;
            
            -- Process the POINT_MSB packet
            ELSIF current_state = SIG_GEN_RECEIVE_RAM_MSB THEN
                
                write_data <= "0000000000000000";
                
                write_data( 15 DOWNTO 8 ) <= packet_contents;
                
                next_state <= SIG_GEN_WAIT_RECEIVE_RAM_LSB;
            
            -- Receive the POINT_LSB packet
            ELSIF current_state = SIG_GEN_WAIT_RECEIVE_RAM_LSB AND 
                  new_data_packet = '1' THEN
            
                next_state <= SIG_GEN_RECEIVE_RAM_LSB;
            
            -- Process the POINT_LSB packet
            ELSIF current_state = SIG_GEN_RECEIVE_RAM_LSB THEN
                
                write_data( 7 DOWNTO 0 ) <= packet_contents;
                
                next_state <= SIG_GEN_STORE_POINT_IN_RAM;
            
            -- Store the new point in memory
            ELSIF current_state = SIG_GEN_STORE_POINT_IN_RAM THEN
            
                points_loaded := points_loaded + 1;
                
                -- Stop receiving data points
                IF points_loaded = NB_OF_POINTS_PER_GROUP THEN
                    
                    next_state <= SIG_GEN_WAIT_START_COM;
                
                -- Receive the next point
                ELSE
                    
                    next_state <= SIG_GEN_WAIT_RECEIVE_RAM_MSB;
                    
                END IF;
                
            -- Stay at the current state
            ELSE
            
                next_state <= next_state;
                
            END IF;
        END IF;
    END PROCESS;
    
    
    -- Prepare the signals for the FT245R USB FIFO chip
    q_usb_rd            <= NOT fifo_reading_data;
    fifo_data_available <= NOT i_usb_rxf;
    
    -- Start the SRAM writing operation when appropriate
    WITH current_state SELECT 
        sram_write <= 
            '1' WHEN SIG_GEN_STORE_POINT_IN_RAM, 
            '0' WHEN OTHERS;
    
    -- Manage the SRAM write address
    PROCESS( clk, current_state, sram_write, write_address, run_generator ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
            -- Reset the SRAM write address
            IF current_state = SIG_GEN_RESET THEN
                
                write_address <= SRAM_FIRST_ADDRESS;
            
            -- Increment the SRAM write address during a write operation   
            ELSIF sram_write = '1' THEN
                
                IF write_address >= SRAM_LAST_ADDRESS THEN
                
                    write_address <= SRAM_FIRST_ADDRESS;
                    
                ELSE 
                
                    write_address <= write_address + 1;
                    
                END IF;
                
            END IF;
        END IF; 
    END PROCESS;
    
    -- Manage the SRAM read address
    PROCESS( clk, run_generator ) IS
    BEGIN
        IF RISING_EDGE( clk ) THEN
            -- Reset the SRAM read address
            IF run_generator = '0' THEN
            
                read_address <= SRAM_FIRST_ADDRESS;
                
                sram_read <= '0';
                
            -- Increment the current SRAM read address
            ELSE
                IF read_address >= ( SRAM_LAST_ADDRESS - read_increment ) THEN
                
                    read_address <= SRAM_FIRST_ADDRESS;
                    
                ELSE 
                
                    read_address <= read_address + read_increment;
                    
                END IF;
                
                sram_read <= '1';
            END IF;
        END IF;
    END PROCESS;
    
    -- Manage the SRAM access mode 
    -- Generator must be stopped to write to SRAM
    WITH run_generator SELECT
        sram_addr <= 
            read_address WHEN '1',
            write_address WHEN OTHERS;
    
    -- Output the selected signal to the DAC
    q_dac_clk <= clk;
    
    WITH run_generator SELECT
        q_dac_data <= 
            -- Apply the selected attenuation to the current SRAM data point
            TO_STDLOGICVECTOR( TO_BITVECTOR(  read_data( 13 DOWNTO 0 ) ) SRL 
                               CONV_INTEGER( attenuation ) ) WHEN '1', 
            "00000000000000" WHEN OTHERS;
    
    
    -- "Power on" LED (always lit)
    q_leds( 0 ) <= '0';
    
    -- "Generator running" LED
    WITH run_generator SELECT
        q_leds( 1 ) <=
            '0' WHEN '1', 
            '1' WHEN OTHERS;
    
    -- "Communication pending" LED
    WITH current_state SELECT
        q_leds( 2 ) <=
            '0' WHEN SIG_GEN_WAIT_RECEIVE_COMMAND, 
            '1' WHEN OTHERS;
    
    -- "Loading points in SRAM" LED
    WITH current_state SELECT
        q_leds( 3 ) <=
            '0' WHEN SIG_GEN_WAIT_RECEIVE_RAM_MSB, 
            '0' WHEN SIG_GEN_RECEIVE_RAM_MSB,
            '0' WHEN SIG_GEN_WAIT_RECEIVE_RAM_LSB, 
            '0' WHEN SIG_GEN_RECEIVE_RAM_LSB, 
            '0' WHEN SIG_GEN_STORE_POINT_IN_RAM, 
            '1' WHEN OTHERS;
    
    -- Always off LEDs
    q_leds( 7 DOWNTO 4 ) <= "1111";
    
END top_level_arch;

