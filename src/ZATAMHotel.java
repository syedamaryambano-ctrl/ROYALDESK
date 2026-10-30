import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class ZATAMHotel extends JFrame {
    private static final Color NAVY = new Color(17, 16, 15);
    private static final Color GOLD = new Color(222, 177, 83);
    private static final Color CREAM = new Color(250, 246, 236);
    private static final Color TEXT = new Color(48, 43, 36);
    private static final String RESERVATION_FILE = "reservations.txt";
    private static final String BACKUP_FILE = "reservations_backup.dat";
    private static final String INVENTORY_FILE = "zatam_inventory_bookings.txt";
    private static final String WAITING_LIST_FILE = "zatam_waiting_list.txt";
    private static final String REVIEWS_FILE = "zatam_reviews.txt";
    private static final String GYM_MEMBERS_FILE = "zatam_public_gym_members.txt";
    private static final int PUBLIC_GYM_CAPACITY_PER_TIME_SLOT = 40;
    private static final String[] PUBLIC_GYM_TIME_SLOTS = {
            "06:00–08:00 AM", "08:00–10:00 AM", "10:00 AM–12:00 PM",
            "12:00–02:00 PM", "02:00–04:00 PM", "04:00–06:00 PM",
            "06:00–08:00 PM", "08:00–10:00 PM"
    };
    private static final String STAFF_ID = "ZATAM-STAFF-2026";

    private final List<BookableItem> bookableItems = new ArrayList<>();
    private final JComboBox<String> itemChoice = new JComboBox<>();
    private final JTextField guestNameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JSpinner checkInField = createDatePicker(LocalDate.now().plusDays(1));
    private final JSpinner checkOutField = createDatePicker(LocalDate.now().plusDays(2));
    private final JComboBox<String> startTimeChoice = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"});
    private final JComboBox<String> endTimeChoice = new JComboBox<>(new String[]{"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"});
    private final JSpinner guestSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JComboBox<String> paymentChoice = new JComboBox<>(new String[]{
            "Pay at hotel (cash)", "Debit/Credit card (demo only)",
            "Bank transfer (demo only)", "JazzCash / Easypaisa (demo only)"
    });
    private final JLabel estimateLabel = new JLabel("Choose an item and dates to see the estimate.");
    private final JTextArea bookingResult = new JTextArea(7, 30);
    private JTabbedPane tabs;

    public ZATAMHotel() {
        super("ZATAM RoyalDesk | Guest & Reservation System");
        createBookableItems();
        UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("SansSerif", Font.BOLD, 13));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // A smaller minimum size lets the interface adapt to compact laptop screens.
        setMinimumSize(new Dimension(760, 560));
        setSize(1280, 850);
        setLocationRelativeTo(null);
        setContentPane(buildMainPanel());
        setVisible(true);
    }

    private void createBookableItems() {
        bookableItems.add(new StandardRoom());
        bookableItems.add(new DeluxeRoom());
        bookableItems.add(new ExecutiveRoom());
        bookableItems.add(new FamilyRoom());
        bookableItems.add(new JuniorSuite());
        bookableItems.add(new PenthouseSuite());
        bookableItems.add(new PresidentialSuite());
        bookableItems.add(new ConferenceVenue());
        bookableItems.add(new BallroomVenue());
        bookableItems.add(new GymVenue());
        for (BookableItem item : bookableItems) itemChoice.addItem(item.getName());
    }

    private JPanel buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);
        root.add(buildHeader(), BorderLayout.NORTH);
        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(CREAM);
        tabs.setForeground(NAVY);
        // Navigation is opened from the compact menu button in the header.
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected int calculateTabAreaHeight(int placement, int runCount, int maxTabHeight) { return 0; }
            @Override protected int calculateTabAreaWidth(int placement, int runCount, int maxTabWidth) { return 0; }
        });
        tabs.addChangeListener(e -> SwingUtilities.invokeLater(() -> {
            if (tabs.getSelectedIndex() == 5) tabs.setComponentAt(5, buildInventoryPage());
            if (tabs.getSelectedIndex() == 6) tabs.setComponentAt(6, buildStaffPage());
            if (tabs.getSelectedIndex() == 7) tabs.setComponentAt(7, buildReviewsPage());
            resetSelectedPageScroll();
        }));
        tabs.addTab("Welcome", buildWelcomePage());
        tabs.addTab("Explore Rooms", buildRoomsPage());
        tabs.addTab("Hotel & Facilities", buildFacilitiesPage());
        tabs.addTab("Events & Venues", buildEventsPage());
        tabs.addTab("Make a Reservation", buildBookingPage());
        tabs.addTab("Room & Venue Inventory", buildInventoryPage());
        tabs.addTab("Hotel Staff", buildStaffPage());
        tabs.addTab("Visitor Reviews", buildReviewsPage());
        tabs.addTab("Contact RoyalDesk", buildContactPage());
        tabs.addTab("Public Gym Reservation", buildPublicGymReservationPage());
        root.add(tabs, BorderLayout.CENTER);
        JLabel footer = new JLabel("FOUR QUEENS. ONE CROWN. ONE LEGACY.     •     ZEST, AMBIENCE, TRADITION, AMBITION, MAJESTY", SwingConstants.CENTER);
        footer.setFont(new Font("Serif", Font.ITALIC, 13));
        footer.setForeground(new Color(111, 78, 28));
        footer.setBorder(new EmptyBorder(10, 4, 10, 4));
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 4));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel brand = new JLabel("♛  ZATAM");
        brand.setForeground(GOLD);
        brand.setFont(new Font("Serif", Font.BOLD, 30));
        JLabel subtitle = new JLabel("ROYALDESK  •  GUEST & RESERVATION SYSTEM");
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        left.add(brand);
        left.add(subtitle);
        JLabel tagline = new JLabel("FOUR QUEENS. ONE ROYAL EXPERIENCE.");
        tagline.setForeground(GOLD);
        tagline.setFont(new Font("Serif", Font.BOLD, 13));
        tagline.setHorizontalAlignment(SwingConstants.RIGHT);
        JButton menuButton = new JButton("☰");
        menuButton.setToolTipText("Open RoyalDesk navigation");
        menuButton.setFont(new Font("SansSerif", Font.BOLD, 25));
        menuButton.setForeground(GOLD);
        menuButton.setBackground(new Color(31, 31, 31));
        menuButton.setBorder(new CompoundBorder(new LineBorder(GOLD, 1), new EmptyBorder(7, 13, 7, 13)));
        menuButton.setFocusPainted(false);
        menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel brandAndMenu = new JPanel(new BorderLayout(14, 0));
        brandAndMenu.setOpaque(false);
        brandAndMenu.add(menuButton, BorderLayout.WEST);
        brandAndMenu.add(left, BorderLayout.CENTER);
        JPopupMenu navigation = new JPopupMenu();
        String[] navNames = {"Welcome", "Explore Rooms", "Hotel & Facilities", "Events & Venues", "Make a Reservation", "Room & Venue Inventory", "Hotel Staff", "Visitor Reviews", "Contact RoyalDesk", "Public Gym Reservation"};
        for (int i = 0; i < navNames.length; i++) {
            final int target = i;
            JMenuItem item = new JMenuItem(navNames[i]);
            item.setFont(new Font("SansSerif", Font.BOLD, 13));
            item.addActionListener(e -> tabs.setSelectedIndex(target));
            navigation.add(item);
        }
        menuButton.addActionListener(e -> navigation.show(menuButton, 0, menuButton.getHeight()));
        header.add(brandAndMenu, BorderLayout.WEST);
        header.add(tagline, BorderLayout.EAST);
        // Hide the optional tagline when the window is narrow so it never collides with the brand.
        header.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                tagline.setVisible(header.getWidth() >= 980);
            }
        });
        return header;
    }

    private JPanel buildPublicGymReservationPage() {
        JPanel page = new JPanel(new BorderLayout(18, 18));
        page.setBackground(CREAM);
        page.setBorder(new EmptyBorder(28, 32, 28, 32));

        JPanel hero = new JPanel(new BorderLayout(10, 12));
        hero.setBackground(NAVY);
        hero.setBorder(new CompoundBorder(new LineBorder(GOLD, 2), new EmptyBorder(28, 30, 28, 30)));
        JLabel eyebrow = new JLabel("♛  ZATAM WELLNESS CLUB");
        eyebrow.setForeground(GOLD);
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel title = new JLabel("PUBLIC GYM RESERVATION");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        JLabel description = new JLabel("Reserve your membership and preferred training time slot.");
        description.setForeground(new Color(239, 229, 207));
        description.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JPanel headings = new JPanel(new GridLayout(0, 1, 0, 9));
        headings.setOpaque(false);
        headings.add(eyebrow);
        headings.add(title);
        headings.add(description);
        hero.add(headings, BorderLayout.CENTER);

        JPanel details = new JPanel(new GridLayout(0, 1, 0, 12));
        details.setBackground(CREAM);
        JLabel capacity = new JLabel("✦  LIMITED CAPACITY  •  40 MEMBERS PER TIME SLOT");
        capacity.setFont(new Font("SansSerif", Font.BOLD, 15));
        capacity.setForeground(new Color(126, 82, 22));
        JLabel membership = new JLabel("✦  MEMBERSHIP PERIOD  •  1 TO 24 MONTHS");
        membership.setFont(new Font("SansSerif", Font.BOLD, 15));
        membership.setForeground(TEXT);
        JLabel price = new JLabel("✦  MEMBERSHIP RATE  •  PKR 10,000 PER MONTH");
        price.setFont(new Font("SansSerif", Font.BOLD, 15));
        price.setForeground(TEXT);
        details.add(capacity);
        details.add(membership);
        details.add(price);

        JButton reserve = new JButton("RESERVE PUBLIC GYM MEMBERSHIP  →");
        reserve.setFont(new Font("SansSerif", Font.BOLD, 16));
        reserve.setForeground(NAVY);
        reserve.setBackground(GOLD);
        reserve.setFocusPainted(false);
        reserve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reserve.setBorder(new CompoundBorder(new LineBorder(new Color(164, 119, 42), 1), new EmptyBorder(16, 22, 16, 22)));
        reserve.addActionListener(e -> enrollPublicGymMember());
        JPanel action = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        action.setOpaque(false);
        action.add(reserve);

        JPanel lower = new JPanel(new BorderLayout(10, 22));
        lower.setOpaque(false);
        lower.add(details, BorderLayout.CENTER);
        lower.add(action, BorderLayout.SOUTH);
        page.add(hero, BorderLayout.NORTH);
        page.add(lower, BorderLayout.CENTER);
        return page;
    }

    // Welcome page: poster, then large DAY and NIGHT hotel views stacked vertically.
    private JPanel buildWelcomePage() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(14, 8, 24, 8));

        JPanel posterHolder = new JPanel(new BorderLayout());
        posterHolder.setBackground(Color.BLACK);
        posterHolder.setBorder(new LineBorder(GOLD, 2));
        posterHolder.setAlignmentX(Component.CENTER_ALIGNMENT);
        posterHolder.setBorder(new CompoundBorder(new LineBorder(GOLD, 2), new EmptyBorder(5, 5, 5, 5)));

        ImageIcon poster = null;
        String[] posterPaths = {
                "pics/POSTER/ZATAM BY 4 QUEENS.png",
                "POSTER/ZATAM BY 4 QUEENS.png",
                "pics/zatam_poster.png",
                "zatam_poster.png"
        };
        for (String path : posterPaths) {
            poster = loadScaledImage(path, 980, 1100);
            if (poster != null) break;
        }
        if (poster != null) {
            posterHolder.add(new JLabel(poster, SwingConstants.CENTER), BorderLayout.CENTER);
            posterHolder.setPreferredSize(new Dimension(poster.getIconWidth() + 4, poster.getIconHeight() + 4));
        } else {
            JLabel posterText = new JLabel("<html><div style='text-align:center;color:#DEB153;padding:35px'>"
                    + "<h1 style='font-size:42px'>♛ ZATAM ♛</h1><h2>GRAND LUXURY HOTEL</h2>"
                    + "<h3>FOUR QUEENS. ONE ROYAL EXPERIENCE.</h3><br>"
                    + "Z — Zara: Grace &amp; Vision<br>A — Ayesha: Strength &amp; Elegance<br>"
                    + "T — Tasreeba: Unity &amp; Ambition<br>M — Maryam: Royal Spirit &amp; Grace</div></html>",
                    SwingConstants.CENTER);
            posterHolder.add(posterText, BorderLayout.CENTER);
            posterHolder.setPreferredSize(new Dimension(980, 420));
        }
        posterHolder.setMaximumSize(new Dimension(1400, posterHolder.getPreferredSize().height));

        JPanel welcomeIntroPanel = new JPanel();
        welcomeIntroPanel.setLayout(new BoxLayout(welcomeIntroPanel, BoxLayout.Y_AXIS));
        welcomeIntroPanel.setBackground(new Color(250, 246, 236));
        welcomeIntroPanel.setBorder(new CompoundBorder(new LineBorder(new Color(210, 176, 102), 1),
                new EmptyBorder(20, 16, 20, 16)));
        welcomeIntroPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeIntroPanel.setMaximumSize(new Dimension(1400, 150));

        JLabel welcomeTitle = new JLabel("Welcome to ZATAM Grand Luxury Hotel", SwingConstants.CENTER);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeTitle.setFont(new Font("Serif", Font.BOLD, 30));
        welcomeTitle.setForeground(NAVY);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel intro = new JLabel("Four Queens. One Royal Experience. Explore our rooms, facilities and grand spaces.", SwingConstants.CENTER);
        intro.setAlignmentX(Component.CENTER_ALIGNMENT);
        intro.setFont(new Font("SansSerif", Font.PLAIN, 16));
        intro.setForeground(TEXT);
        intro.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeIntroPanel.add(welcomeTitle);
        welcomeIntroPanel.add(Box.createVerticalStrut(9));
        welcomeIntroPanel.add(intro);

        JLabel hotelViewTitle = new JLabel("Discover ZATAM — Day & Night", SwingConstants.CENTER);
        hotelViewTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        hotelViewTitle.setFont(new Font("Serif", Font.BOLD, 28));
        hotelViewTitle.setForeground(NAVY);
        hotelViewTitle.setBorder(new EmptyBorder(8, 0, 8, 0));

        // Vertical stack: users scroll down from the poster to the day and night photos.
        JPanel viewsPanel = new JPanel();
        viewsPanel.setLayout(new BoxLayout(viewsPanel, BoxLayout.Y_AXIS));
        viewsPanel.setBackground(CREAM);
        viewsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewsPanel.setMaximumSize(new Dimension(1400, Integer.MAX_VALUE));

        viewsPanel.add(createWelcomeViewCard("DAY VIEW", new String[]{
                "pics/complete view/day view.png",
                "pics/complete view/day view.jpg",
                "pics/complete view/day view.jpeg",
                "pics/complete view/day view.PNG"
        }));
        viewsPanel.add(Box.createVerticalStrut(22));
        viewsPanel.add(createWelcomeViewCard("NIGHT VIEW", new String[]{
                "pics/complete view/night view.png",
                "pics/complete view/night view.jpg",
                "pics/complete view/night view.jpeg",
                "pics/complete view/night view.PNG"
        }));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setBorder(new EmptyBorder(8, 0, 4, 0));
        JButton explore = goldButton("Explore Rooms");
        explore.addActionListener(e -> tabs.setSelectedIndex(1));
        JButton reserve = darkButton("Book Your Stay");
        reserve.addActionListener(e -> tabs.setSelectedIndex(4));
        buttons.add(explore);
        buttons.add(reserve);

        content.add(posterHolder);
        content.add(Box.createVerticalStrut(22));
        content.add(welcomeIntroPanel);
        content.add(Box.createVerticalStrut(24));
        content.add(hotelViewTitle);
        content.add(Box.createVerticalStrut(14));
        content.add(viewsPanel);
        content.add(Box.createVerticalStrut(18));
        content.add(buttons);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getViewport().setBackground(CREAM);
        return wrapWelcomeScroll(scroll);
    }

    private JPanel createWelcomeViewCard(String title, String[] paths) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(192, 151, 69), 2), new EmptyBorder(8, 8, 8, 8)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(1400, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel("✦  " + title + "  ✦", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 23));
        titleLabel.setForeground(NAVY);
        titleLabel.setBorder(new EmptyBorder(3, 0, 5, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        ImageIcon view = null;
        String foundPath = null;
        for (String path : paths) {
            view = loadPhotoFit(path, 1300, 760);
            if (view != null) { foundPath = path; break; }
        }
        JLabel imageLabel = new JLabel(view == null ? "IMAGE UNAVAILABLE — CHECK THE PICTURE FOLDER" : "",
                SwingConstants.CENTER);
        imageLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        imageLabel.setForeground(TEXT);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(237, 229, 211));
        if (view != null) imageLabel.setIcon(view);
        imageLabel.setBorder(new LineBorder(new Color(220, 205, 174), 1));
        card.add(imageLabel, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(1300, (view == null ? 220 : view.getIconHeight() + 45)));
        card.setMaximumSize(new Dimension(1400, (view == null ? 220 : view.getIconHeight() + 45)));
        if (foundPath != null) {
            final String imagePath = foundPath;
            imageLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    int w = Math.max(280, Math.min(1300, imageLabel.getWidth() - 4));
                    int h = Math.max(180, Math.min(760, (int)(w * 0.58)));
                    ImageIcon resized = loadScaledImage(imagePath, w, h);
                    if (resized != null) imageLabel.setIcon(resized);
                }
            });
        }
        return card;
    }

    private JPanel wrapWelcomeScroll(JScrollPane scroll) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRoomsPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("Explore Our Rooms & Suites", "Click a room's photo or gallery button to see its bedroom, washroom, balcony and sitting-area photos.");
        // One full-width room card per row prevents the photo, text and buttons from colliding.
        GridLayout roomGrid = new GridLayout(0, 1, 18, 18);
        JPanel cards = new JPanel(roomGrid);
        cards.setBackground(CREAM);
        cards.setBorder(new EmptyBorder(8, 8, 12, 8));
        for (BookableItem item : bookableItems) {
            if (item instanceof Room) cards.add(createItemCard(item, true));
        }
        JScrollPane scroll = new JScrollPane(cards);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        page.add(heading, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel createItemCard(BookableItem item, boolean roomCard) {
        // Vertical card layout: photo first, description underneath, then price and buttons.
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(222, 205, 166), 1),
                new EmptyBorder(12, 14, 12, 14)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        String firstPath = item.getImagePaths().length > 0 ? item.getImagePaths()[0] : "";
        // Restore the original proportional image loader so room photos display normally.
        ImageIcon image = loadScaledImage(firstPath, 1350, 520);
        JLabel imageLabel = new JLabel(
                image == null ? "Photo will appear when its file path matches" : "",
                SwingConstants.CENTER);
        if (image != null) imageLabel.setIcon(image);
        imageLabel.setOpaque(true);
        if (image == null) imageLabel.setBackground(new Color(237, 229, 211));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(1300, image == null ? 240 : image.getIconHeight()));
        imageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, image == null ? 240 : image.getIconHeight()));
        imageLabel.setMinimumSize(new Dimension(200, image == null ? 200 : image.getIconHeight()));
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { showGallery(item); }
        });
        if (!firstPath.isEmpty()) {
            imageLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    int availableWidth = Math.max(280, Math.min(1350, imageLabel.getWidth() - 4));
                    ImageIcon resized = loadScaledImage(firstPath, availableWidth, 520);
                    if (resized != null && (imageLabel.getIcon() == null
                            || imageLabel.getIcon().getIconWidth() != resized.getIconWidth()
                            || imageLabel.getIcon().getIconHeight() != resized.getIconHeight())) {
                        imageLabel.setIcon(resized);
                        imageLabel.setPreferredSize(new Dimension(availableWidth, resized.getIconHeight()));
                        imageLabel.revalidate();
                    }
                }
            });
        }

        JLabel name = new JLabel(item.getName().toUpperCase());
        name.setFont(new Font("Serif", Font.BOLD, 23));
        name.setForeground(NAVY);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Center the description underneath the image, with centered text.
        JTextPane description = new JTextPane();
        description.setText(item.getDescription());
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setFont(new Font("SansSerif", Font.PLAIN, 14));
        description.setBorder(new EmptyBorder(4, 12, 4, 12));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Give the description enough vertical room so the final line is never clipped.
        description.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        javax.swing.text.SimpleAttributeSet centeredText = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(centeredText, javax.swing.text.StyleConstants.ALIGN_CENTER);
        description.getStyledDocument().setParagraphAttributes(0,
                description.getDocument().getLength(), centeredText, false);
        description.setPreferredSize(new Dimension(1000, 125));

        JLabel price = new JLabel("PKR " + money(item.getPricePerNight())
                + (roomCard ? " / NIGHT" : " / EVENT-DAY"));
        price.setFont(new Font("SansSerif", Font.BOLD, 16));
        price.setForeground(new Color(132, 86, 21));
        price.setHorizontalAlignment(SwingConstants.CENTER);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton gallery = goldButton("VIEW PHOTOS");
        gallery.addActionListener(e -> showGallery(item));
        JButton book = darkButton(roomCard ? "BOOK THIS ROOM" : "BOOK THIS VENUE");
        book.addActionListener(e -> openBookingFor(item));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        actions.add(gallery);
        actions.add(book);

        card.add(imageLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(name);
        card.add(Box.createVerticalStrut(6));
        card.add(description);
        card.add(Box.createVerticalStrut(8));
        card.add(price);
        card.add(Box.createVerticalStrut(10));
        card.add(actions);
        return card;
    }

    private JPanel buildFacilitiesPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("Discover the Hotel", "Open any gallery to view the hotel's entrance, day and night views, lobby, pool, dining, conference rooms and ballrooms.");
        GridLayout facilityGrid = new GridLayout(0, 2, 18, 18);
        JPanel galleryCards = new JPanel(facilityGrid);
        galleryCards.setBackground(CREAM);
        galleryCards.setBorder(new EmptyBorder(8, 8, 12, 8));
        galleryCards.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int columns = galleryCards.getWidth() < 820 ? 1 : 2;
                if (facilityGrid.getColumns() != columns) { facilityGrid.setColumns(columns); galleryCards.revalidate(); }
            }
        });
        galleryCards.add(createFacilityCard("Hotel Entrance — Day & Night", "pics/entrance/entrance in morning.png", new String[]{"pics/entrance/entrance in morning.png", "pics/entrance/entrance at night.png"}));
        galleryCards.add(createFacilityCard("Complete Hotel Views", "pics/complete view/day view.png", new String[]{"pics/complete view/day view.png", "pics/complete view/night view.png"}));
        galleryCards.add(createFacilityCard("Lobby & Reception", "pics/lobby and reception area/lobby 1.jpg", new String[]{"pics/lobby and reception area/lobby 1.jpg", "pics/lobby and reception area/lobby 2.jpg", "pics/lobby and reception area/lobby 3.jpg"}));
        galleryCards.add(createFacilityCard("Gym & Fitness Center", "pics/GYM/gym.png", new String[]{"pics/GYM/gym.png", "pics/GYM/gymm.jpg"}));
        galleryCards.add(createFacilityCard("Pool Area", "pics/pool area/private pool.jpg", new String[]{"pics/pool area/private pool.jpg", "pics/pool area/pool 1.jpg", "pics/pool area/pool 2.jpg", "pics/pool area/pool 3.jpg", "pics/pool area/pool 4.jpg", "pics/pool area/pool 5.jpg", "pics/pool area/pool 6.jpg", "pics/pool area/pool 7.jpg"}));
        galleryCards.add(createFacilityCard("Dining & Restaurant", "pics/dinning/public dinning.jpg", new String[]{"pics/dinning/public dinning.jpg", "pics/dinning/dinning 1.jpg", "pics/dinning/dinning 2.jpg", "pics/dinning/dinning 3.jpg", "pics/dinning/dinning 4.jpg", "pics/dinning/dinning 5.jpg", "pics/dinning/dinning 6.jpg", "pics/dinning/dinning 7.jpg"}));
        galleryCards.add(createFacilityCard("Conference Rooms", "pics/conference rooms/room 1.jpg", new String[]{"pics/conference rooms/room 1.jpg", "pics/conference rooms/room2.jpg", "pics/conference rooms/hall.jpg", "pics/conference rooms/• Auditórium.jpg"}));
        galleryCards.add(createFacilityCard("Ballrooms", "pics/ball rooms/ball room1.jpg", new String[]{"pics/ball rooms/ball room1.jpg", "pics/ball rooms/ball room 2.jpg", "pics/ball rooms/ball room 3.jpg", "pics/ball rooms/ball room 4.jpg", "pics/ball rooms/ball room 5.jpg", "pics/ball rooms/ball room 6.jpg", "pics/ball rooms/ball room 7.jpg"}));
        JScrollPane scroll = new JScrollPane(galleryCards);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        page.add(heading, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel createFacilityCard(String title, String previewPath, String[] galleryPaths) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(222, 205, 166)), new EmptyBorder(10, 10, 10, 10)));
        ImageIcon icon = loadScaledImage(previewPath, 500, 210);
        JLabel photo = new JLabel(icon == null ? "Hotel photo" : "", SwingConstants.CENTER);
        if (icon != null) photo.setIcon(icon);
        else { photo.setOpaque(true); photo.setBackground(new Color(237, 229, 211)); }
        JLabel name = new JLabel(title);
        name.setFont(new Font("Serif", Font.BOLD, 20));
        name.setForeground(NAVY);
        JButton view = goldButton("View Gallery");
        view.addActionListener(e -> showGallery(title, "Hotel facility gallery.", galleryPaths));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(name, BorderLayout.CENTER);
        bottom.add(view, BorderLayout.EAST);
        card.add(photo, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildEventsPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("Meetings, Celebrations & Events",
                "Explore our event spaces: professional conference rooms, elegant ballrooms, and private gym facilities.");
        // Keep Events & Venues in the same two-column card layout as Hotel & Facilities.
        GridLayout eventGrid = new GridLayout(0, 2, 18, 18);
        JPanel cards = new JPanel(eventGrid);
        cards.setBackground(CREAM);
        cards.setBorder(new EmptyBorder(8, 8, 12, 8));
        cards.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int columns = cards.getWidth() < 820 ? 1 : 2;
                if (eventGrid.getColumns() != columns) {
                    eventGrid.setColumns(columns);
                    cards.revalidate();
                }
            }
        });
        for (BookableItem item : bookableItems) {
            if (item instanceof Venue || item instanceof GymVenue) cards.add(createEventCard(item));
        }
        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(heading, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        JTextArea note = new JTextArea(
                "Booking note: conference rooms, ballrooms and private gym sessions are scheduled by time. Public gym membership is managed separately.");
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setFocusable(false);
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        note.setForeground(TEXT);
        note.setBackground(new Color(250, 246, 237));
        note.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(222, 205, 166)), new EmptyBorder(10, 12, 10, 12)));
        page.add(note, BorderLayout.SOUTH);
        return page;
    }

    // Event cards match the Facilities page: large photo with title and actions along the bottom.
    private JPanel createEventCard(BookableItem item) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(222, 205, 166)), new EmptyBorder(10, 10, 10, 10)));

        String[] paths = item.getImagePaths();
        String previewPath = paths.length > 0 ? paths[0] : "";
        ImageIcon icon = loadScaledImage(previewPath, 700, 320);
        JLabel photo = new JLabel(icon == null ? "Venue photo" : "", SwingConstants.CENTER);
        photo.setOpaque(true);
        photo.setBackground(new Color(237, 229, 211));
        if (icon != null) photo.setIcon(icon);
        photo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        photo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { showGallery(item); }
        });

        JLabel name = new JLabel(item.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Serif", Font.BOLD, 20));
        name.setForeground(NAVY);
        JLabel price = new JLabel("PKR " + money(item.getPricePerNight()) + " / event-day", SwingConstants.CENTER);
        price.setFont(new Font("SansSerif", Font.BOLD, 14));
        price.setForeground(new Color(132, 86, 21));

        JButton view = goldButton("View Gallery");
        view.addActionListener(e -> showGallery(item));
        JButton book = darkButton("Book This Venue");
        book.addActionListener(e -> openBookingFor(item));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(view);
        actions.add(book);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setOpaque(false);
        bottom.add(name, BorderLayout.NORTH);
        bottom.add(price, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        card.add(photo, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBookingPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("Make a Reservation", "Reserve a room, suite, conference room or ballroom. All prices are example rates for this student project.");
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(222, 205, 166)), new EmptyBorder(18, 20, 18, 20)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 7, 8, 7);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        guestNameField.setPreferredSize(new Dimension(360, 40));
        phoneField.setPreferredSize(new Dimension(360, 40));
        itemChoice.setPreferredSize(new Dimension(360, 40));
        paymentChoice.setPreferredSize(new Dimension(360, 40));
        checkInField.setPreferredSize(new Dimension(220, 40));
        checkOutField.setPreferredSize(new Dimension(220, 40));
        guestSpinner.setPreferredSize(new Dimension(140, 40));
        startTimeChoice.setPreferredSize(new Dimension(220, 40));
        endTimeChoice.setPreferredSize(new Dimension(220, 40));
        addFormRow(form, g, 0, "GUEST / EVENT CONTACT NAME:", guestNameField);
        addFormRow(form, g, 1, "PHONE NUMBER:", phoneField);
        addFormRow(form, g, 2, "Room or venue:", itemChoice);
        addFormRow(form, g, 3, "Check-in / event date (choose date):", checkInField);
        addFormRow(form, g, 4, "Check-out (choose date):", checkOutField);
        addFormRow(form, g, 5, "Guests / attendees:", guestSpinner);
        addFormRow(form, g, 6, "Start time (conference / ballroom / gym):", startTimeChoice);
        addFormRow(form, g, 7, "End time (conference / ballroom / gym):", endTimeChoice);
        addFormRow(form, g, 8, "Payment method:", paymentChoice);
        itemChoice.addActionListener(e -> {
            updateBookingTimeVisibility(form);
            adjustOneDayCheckout();
            updateEstimate();
        });
        updateBookingTimeVisibility(form);
        startTimeChoice.addActionListener(e -> updateEstimate());
        endTimeChoice.addActionListener(e -> updateEstimate());
        checkInField.addChangeListener(e -> {
            adjustOneDayCheckout();
            updateEstimate();
        });
        checkOutField.addChangeListener(e -> updateEstimate());

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        left.add(heading, BorderLayout.NORTH);
        left.add(form, BorderLayout.CENTER);
        JButton confirm = goldButton("Confirm Reservation");
        confirm.addActionListener(e -> saveBooking());
        JButton saved = darkButton("Find My Reservation");
        saved.addActionListener(e -> showGuestOwnBooking());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        buttons.setOpaque(false);
        buttons.add(confirm);
        buttons.add(saved);
        left.add(buttons, BorderLayout.SOUTH);

        JPanel summary = new JPanel(new BorderLayout(8, 8));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(222, 205, 166)), new EmptyBorder(16, 16, 16, 16)));
        JLabel summaryHeading = new JLabel("Reservation Summary");
        summaryHeading.setFont(new Font("Serif", Font.BOLD, 22));
        summaryHeading.setForeground(NAVY);
        summary.add(summaryHeading, BorderLayout.NORTH);
        estimateLabel.setVerticalAlignment(SwingConstants.TOP);
        summary.add(estimateLabel, BorderLayout.CENTER);
        bookingResult.setEditable(false);
        bookingResult.setLineWrap(true);
        bookingResult.setWrapStyleWord(true);
        bookingResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bookingResult.setBackground(new Color(249, 245, 235));
        summary.add(new JScrollPane(bookingResult), BorderLayout.SOUTH);
        summary.setPreferredSize(new Dimension(390, 460));
        summary.setMinimumSize(new Dimension(250, 300));
        JPanel bookingBody = new JPanel(new GridBagLayout());
        bookingBody.setOpaque(false);
        GridBagConstraints body = new GridBagConstraints();
        body.insets = new Insets(0, 0, 0, 14); body.fill = GridBagConstraints.BOTH; body.weightx = 0.72; body.weighty = 1; body.gridx = 0; body.gridy = 0;
        bookingBody.add(left, body);
        body.gridx = 1; body.weightx = 0.28; body.insets = new Insets(0, 0, 0, 0);
        bookingBody.add(summary, body);
        page.add(bookingBody, BorderLayout.CENTER);
        updateEstimate();
        return page;
    }

    private void updateBookingTimeVisibility(JPanel form) {
        boolean show = getSelectedItem() instanceof Venue || getSelectedItem() instanceof GymVenue;
        Component[] components = form.getComponents();
        for (int i=0; i<components.length-1; i++) {
            if (components[i] instanceof JLabel) {
                String text=((JLabel)components[i]).getText();
                if (text.startsWith("Start time") || text.startsWith("End time")) {
                    components[i].setVisible(show); components[i+1].setVisible(show);
                }
            }
        }
        form.revalidate(); form.repaint();
    }

    private void showGallery(BookableItem item) {
        showGallery(item.getName(), item.getDescription(), item.getImagePaths());
    }

    private void showGallery(String title, String description, String[] paths) {
        JDialog dialog = new JDialog(this, title + " — Photo Gallery", true);
        dialog.setSize(900, 650);
        dialog.setLocationRelativeTo(this);
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(NAVY);
        JTextArea desc = new JTextArea(description);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setRows(3);
        desc.setBackground(CREAM);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.setOpaque(false);
        top.add(titleLabel, BorderLayout.NORTH);
        top.add(desc, BorderLayout.CENTER);

        JPanel images = new JPanel(new GridLayout(0, 2, 12, 12));
        images.setBackground(CREAM);
        int found = 0;
        for (String path : paths) {
            ImageIcon icon = loadScaledImage(path, 390, 220);
            if (icon != null) {
                JPanel tile = new JPanel(new BorderLayout(4, 4));
                tile.setBackground(Color.WHITE);
                tile.setBorder(new EmptyBorder(6, 6, 6, 6));
                JLabel picture = new JLabel(icon, SwingConstants.CENTER);
                picture.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                picture.setToolTipText("Click to view this photo in a larger size");
                picture.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showLargeImage(path, title);
                    }
                });

                JLabel fileName = new JLabel(stylishPhotoCaption(title, path) + "  •  CLICK TO ENLARGE",
                        SwingConstants.CENTER);
                fileName.setForeground(new Color(132, 86, 21));
                fileName.setFont(new Font("Serif", Font.BOLD, 14));
                fileName.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                fileName.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showLargeImage(path, title);
                    }
                });

                tile.add(picture, BorderLayout.CENTER);
                tile.add(fileName, BorderLayout.SOUTH);
                tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tile.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showLargeImage(path, title);
                    }
                });
                images.add(tile);
                found++;
            }
        }
        if (found == 0) {
            JLabel missing = new JLabel("No matching photos were found. Check the image paths and filenames in your pics folder.", SwingConstants.CENTER);
            missing.setForeground(new Color(150, 65, 40));
            images.add(missing);
        }
        JScrollPane scroll = new JScrollPane(images);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JButton close = goldButton("Close Gallery");
        close.addActionListener(e -> dialog.dispose());
        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(close, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setVisible(true);
    }


    // Opens the selected gallery photo in a large, clear view.
    private void showLargeImage(String imagePath, String galleryTitle) {
        File file = new File(imagePath);
        if (!file.exists() || !file.isFile()) {
            JOptionPane.showMessageDialog(this,
                    "This image could not be found:\n" + imagePath,
                    "Image not found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ImageIcon original = new ImageIcon(imagePath);
        if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
            JOptionPane.showMessageDialog(this, "This image could not be opened.",
                    "Image error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = Math.min(1200, screen.width - 100);
        int maxHeight = Math.min(800, screen.height - 140);
        double scale = Math.min((double) maxWidth / original.getIconWidth(),
                (double) maxHeight / original.getIconHeight());
        scale = Math.min(scale, 1.0);

        int imageWidth = Math.max(1, (int) (original.getIconWidth() * scale));
        int imageHeight = Math.max(1, (int) (original.getIconHeight() * scale));
        Image scaled = original.getImage().getScaledInstance(
                imageWidth, imageHeight, Image.SCALE_SMOOTH);

        JDialog imageDialog = new JDialog(this, galleryTitle + " — Photo View", true);
        imageDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        imageDialog.setLayout(new BorderLayout(10, 10));
        imageDialog.getContentPane().setBackground(new Color(18, 17, 16));

        JLabel imageLabel = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 145, 62), 2),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel nameLabel = new JLabel(stylishPhotoCaption(galleryTitle, imagePath), SwingConstants.CENTER);
        nameLabel.setForeground(GOLD);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 18));
        nameLabel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton closeButton = goldButton("Close Photo");
        closeButton.addActionListener(e -> imageDialog.dispose());

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(NAVY);
        bottom.setBorder(new EmptyBorder(4, 12, 12, 12));
        bottom.add(nameLabel, BorderLayout.CENTER);
        bottom.add(closeButton, BorderLayout.SOUTH);

        imageDialog.add(imageLabel, BorderLayout.CENTER);
        imageDialog.add(bottom, BorderLayout.SOUTH);
        imageDialog.pack();
        imageDialog.setMinimumSize(new Dimension(500, 350));
        // Center the enlarged photo on the whole screen, not relative to the main window.
        imageDialog.setLocationRelativeTo(null);
        imageDialog.getRootPane().setBorder(new LineBorder(new Color(190, 145, 62), 2));
        imageDialog.setVisible(true);
    }

    private void openBookingFor(BookableItem item) {
        itemChoice.setSelectedItem(item.getName());
        tabs.setSelectedIndex(4);
        updateEstimate();
    }

    private void saveBooking() {
        try {
            String guest = guestNameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (guest.isEmpty() || phone.isEmpty()) throw new IllegalArgumentException("Enter the guest/event contact name and phone number.");
            if (!phone.matches("[0-9+() -]{7,20}")) throw new IllegalArgumentException("Please enter a valid phone number.");
            LocalDate start = selectedDate(checkInField);
            LocalDate end = selectedDate(checkOutField);
            if (start.isBefore(LocalDate.now())) throw new IllegalArgumentException("The date cannot be in the past.");
            if (!end.isAfter(start)) throw new IllegalArgumentException("Check-out must be after check-in. For a one-day event, choose the next day as check-out.");
            BookableItem item = getSelectedItem();
            // Conference, ballroom, and private gym reservations are limited to one calendar day.
            if ((item instanceof ConferenceVenue || item instanceof BallroomVenue || item instanceof GymVenue)
                    && !end.equals(start.plusDays(1))) {
                throw new IllegalArgumentException(item.getName() + " reservations can only be booked for one day. Set check-out to " + start.plusDays(1) + ".");
            }
            int guests = (Integer) guestSpinner.getValue();
            if (guests > item.getMaxGuests()) throw new IllegalArgumentException(item.getName() + " allows up to " + item.getMaxGuests() + " guests/attendees.");
            String startTime = (String) startTimeChoice.getSelectedItem();
            String endTime = (String) endTimeChoice.getSelectedItem();
            boolean timedBooking = item instanceof Venue || item instanceof GymVenue;
            boolean venue = item instanceof Venue;
            if (timedBooking && startTime.compareTo(endTime) >= 0) throw new IllegalArgumentException("For a venue booking, end time must be later than start time.");
            long days = ChronoUnit.DAYS.between(start, end);
            double total = item instanceof GymVenue
                    ? java.time.Duration.between(LocalTime.parse(startTime), LocalTime.parse(endTime)).toHours() * item.getPricePerNight()
                    : days * item.getPricePerNight();
            String payment = (String) paymentChoice.getSelectedItem();
            List<String[]> existing = readInventoryRecords();
            int capacity = capacityFor(item);
            String assignedUnit = findAvailableUnit(item, start, end, startTime, endTime, existing, capacity);
            boolean waiting = assignedUnit == null;
            String status = waiting ? "WAITING_LIST" : "CONFIRMED";
            if (waiting) {
                appendRecord(WAITING_LIST_FILE, recordLine(guest, phone, item.getName(), start.toString(), end.toString(), startTime, endTime, "WAITING", payment, guests));
                bookingResult.setText("WAITING LIST ENTRY SAVED\n------------------------------\nContact: " + guest + "\nPhone: " + phone + "\nItem: " + item.getName() + "\nDates: " + start + " to " + end + (timedBooking ? "\nTime: " + startTime + "–" + endTime : "") + "\nStatus: WAITING LIST\nAll units are currently booked for the selected dates/time. Your visitor data has been saved to " + WAITING_LIST_FILE + ".");
                JOptionPane.showMessageDialog(this, "All " + item.getName() + " units are booked for the selected dates/time.\nYour details have been added to the waiting list.", "Added to Waiting List", JOptionPane.INFORMATION_MESSAGE);
                resetBookingFormForNextReservation();
                return;
            }
            // Save the contact fields in every inventory record so later reservations
            // keep their own guest name and phone instead of depending on form contents.
            String record = recordLine(guest, phone, item.getName(), start.toString(), end.toString(), startTime, endTime, assignedUnit, payment, guests);
            if (record.split("\\|", -1).length < 10 || guest.isBlank() || phone.isBlank()) {
                throw new IOException("Guest name and phone number could not be saved. Please enter them again.");
            }
            appendRecord(INVENTORY_FILE, record);
            Booking booking = new Booking(guest, phone, item.getName() + " (Unit " + assignedUnit + ")", start.toString(), end.toString(), guests, days, total, payment);
            FileWriter writer = null;
            try { writer = new FileWriter(RESERVATION_FILE, true); writer.write(booking.toFileLine() + " | Time=" + startTime + "-" + endTime + " | Unit=" + assignedUnit + System.lineSeparator()); }
            finally { if (writer != null) writer.close(); }
            copyFileUsingStreams(RESERVATION_FILE, BACKUP_FILE);
            bookingResult.setText("RESERVATION CONFIRMED\n------------------------------\nContact: " + guest + "\nPhone: " + phone + "\nItem: " + item.getName() + "\nAssigned room/venue number: " + assignedUnit + "\nDates: " + start + " to " + end + (timedBooking ? "\nTime: " + startTime + "–" + endTime : "") + "\nDays/nights: " + days + "\nGuests/attendees: " + guests + "\nEstimated total: PKR " + money(total) + "\nPayment choice: " + payment + "\n\nSaved to " + RESERVATION_FILE + ". Payment is not processed by this demo.");
            JOptionPane.showMessageDialog(this, "Reservation confirmed!\nAssigned number: " + assignedUnit + (timedBooking ? "\nTime: " + startTime + "–" + endTime : "") + "\nEstimated total: PKR " + money(total), "ZATAM Reservation", JOptionPane.INFORMATION_MESSAGE);
            resetBookingFormForNextReservation();
            if (tabs != null) { tabs.setComponentAt(5, buildInventoryPage()); if (tabs.getSelectedIndex() == 5) tabs.revalidate(); tabs.repaint(); }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check your details", JOptionPane.WARNING_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Choose valid check-in and check-out dates using the date selectors.", "Invalid date", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the reservation: " + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
        } finally { System.out.println("Reservation attempt finished."); }
    }

    private int capacityFor(BookableItem item) {
        if (item instanceof StandardRoom || item instanceof DeluxeRoom || item instanceof ExecutiveRoom || item instanceof FamilyRoom || item instanceof JuniorSuite || item instanceof PenthouseSuite || item instanceof PresidentialSuite) return 20;
        if (item instanceof ConferenceVenue) return 4;
        if (item instanceof BallroomVenue) return 6;
        if (item instanceof GymVenue) return 2;
        return 1;
    }

    /** Clears the entry form after saving so the next reservation never reuses old guest data. */
    private void resetBookingFormForNextReservation() {
        guestNameField.setText("");
        phoneField.setText("");
        checkInField.setValue(toDate(LocalDate.now().plusDays(1)));
        checkOutField.setValue(toDate(LocalDate.now().plusDays(2)));
        adjustOneDayCheckout();
        guestSpinner.setValue(1);
        if (startTimeChoice.getItemCount() > 0) startTimeChoice.setSelectedIndex(0);
        if (endTimeChoice.getItemCount() > 0) endTimeChoice.setSelectedIndex(0);
        if (paymentChoice.getItemCount() > 0) paymentChoice.setSelectedIndex(0);
        bookingResult.setText("Ready for the next reservation.\n\nThe previous reservation has been saved. Enter new guest details and dates above.");
        updateEstimate();
        guestNameField.requestFocusInWindow();
    }

    private String recordLine(String guest, String phone, String item, String start, String end, String startTime, String endTime, String unit, String payment, int guests) {
        return String.join("|", clean(guest), clean(phone), clean(item), start, end, startTime, endTime, unit, clean(payment), Integer.toString(guests));
    }
    private String clean(String value) { return value.replace("|", "/").replace("\n", " ").trim(); }
    private void appendRecord(String path, String line) throws IOException { try (FileWriter w = new FileWriter(path, true)) { w.write(line + System.lineSeparator()); } }

    private List<String[]> readInventoryRecords() throws IOException {
        List<String[]> records = new ArrayList<>();
        File f = new File(INVENTORY_FILE);
        if (!f.exists()) return records;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) { String[] parts = line.split("\\|", -1); if (parts.length >= 10) records.add(parts); }
        }
        return records;
    }

    private String findAvailableUnit(BookableItem item, LocalDate start, LocalDate end, String startTime, String endTime, List<String[]> records, int capacity) {
        boolean venue = item instanceof Venue || item instanceof GymVenue;
        for (int n = 1; n <= capacity; n++) {
            String candidate = (venue ? (item instanceof ConferenceVenue ? "CR-" : item instanceof BallroomVenue ? "BR-" : item instanceof GymVenue ? "GY-" : "VN-") : roomPrefix(item)) + String.format("%02d", n);
            boolean occupied = false;
            for (String[] r : records) {
                if (r[2].equals(item.getName()) && r[7].equals(candidate)) {
                    LocalDate oldStart, oldEnd;
                    try { oldStart = LocalDate.parse(r[3]); oldEnd = LocalDate.parse(r[4]); } catch (Exception ex) { continue; }
                    boolean datesOverlap = start.isBefore(oldEnd) && oldStart.isBefore(end);
                    boolean timesOverlap = !(item instanceof Venue || item instanceof GymVenue) || (startTime.compareTo(r[6]) < 0 && r[5].compareTo(endTime) < 0);
                    if (datesOverlap && timesOverlap) { occupied = true; break; }
                }
            }
            if (!occupied) return candidate;
        }
        return null;
    }
    private String roomPrefix(BookableItem item) {
        if (item instanceof StandardRoom) return "ST-"; if (item instanceof DeluxeRoom) return "DL-";
        if (item instanceof ExecutiveRoom) return "EX-"; if (item instanceof FamilyRoom) return "FM-";
        if (item instanceof JuniorSuite) return "JS-"; if (item instanceof PenthouseSuite) return "PH-";
        if (item instanceof PresidentialSuite) return "PS-"; return "RM-";
    }

    private JPanel buildInventoryPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("Room & Venue Inventory", "View total units, current reservations and available units. Click a type to see its occupied unit numbers.");
        JPanel rows = new JPanel(); rows.setBackground(CREAM); rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        for (BookableItem item : bookableItems) {
            JPanel row = new JPanel(new BorderLayout(12, 8)); row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(222,205,166)), new EmptyBorder(12,14,12,14)));
            JLabel label = new JLabel(item.getName()); label.setFont(new Font("Serif", Font.BOLD, 18)); label.setForeground(NAVY);
            JButton details = darkButton("View Occupancy");
            details.addActionListener(e -> showOccupancy(item));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false); right.add(details);
            row.add(label, BorderLayout.WEST); row.add(right, BorderLayout.EAST); row.add(new JLabel(inventorySummary(item)), BorderLayout.CENTER);
            rows.add(row); rows.add(Box.createVerticalStrut(8));
        }
        // Public gym memberships are stored separately from room/venue reservations.
        // Provide an inventory entry so staff can view and cancel those enrollments here too.
        JPanel gymRow = new JPanel(new BorderLayout(12, 8));
        gymRow.setBackground(Color.WHITE);
        gymRow.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(222,205,166)), new EmptyBorder(12,14,12,14)));
        JLabel gymLabel = new JLabel("Public Gym Memberships");
        gymLabel.setFont(new Font("Serif", Font.BOLD, 18));
        gymLabel.setForeground(NAVY);
        JButton gymDetails = darkButton("View Reservations");
        gymDetails.addActionListener(e -> showPublicGymEnrollments(false));
        JPanel gymRight = new JPanel(new FlowLayout(FlowLayout.RIGHT)); gymRight.setOpaque(false); gymRight.add(gymDetails);
        gymRow.add(gymLabel, BorderLayout.WEST);
        gymRow.add(gymRight, BorderLayout.EAST);
        gymRow.add(new JLabel(publicGymInventorySummary()), BorderLayout.CENTER);
        rows.add(gymRow); rows.add(Box.createVerticalStrut(8));
        JScrollPane scroll = new JScrollPane(rows); scroll.getVerticalScrollBar().setUnitIncrement(16); scroll.setBorder(BorderFactory.createEmptyBorder());
        page.add(heading, BorderLayout.NORTH); page.add(scroll, BorderLayout.CENTER); return page;
    }
    private String publicGymInventorySummary() {
        int total = 0;
        File file = new File(GYM_MEMBERS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (reader.readLine() != null) total++;
            } catch (IOException ex) {
                return "Could not read public gym memberships: " + ex.getMessage();
            }
        }
        return "Total memberships: " + total + "  |  View Reservations displays member names only; staff can open full details in the Staff Dashboard.";
    }

    private boolean isRecordActiveToday(String[] r) { try { LocalDate a=LocalDate.parse(r[3]), b=LocalDate.parse(r[4]), today=LocalDate.now(); return !today.isBefore(a) && today.isBefore(b); } catch(Exception ex) { return false; } }

    private String inventorySummary(BookableItem item) {
        int total = capacityFor(item);
        java.util.Set<String> reservedUnits = new java.util.HashSet<>();
        try {
            for (String[] r : readInventoryRecords()) {
                if (r.length > 7 && r[2].equals(item.getName()) && !r[7].isBlank()
                        && !r[7].equalsIgnoreCase("WAITING") && !r[7].equalsIgnoreCase("CANCELLED")) {
                    reservedUnits.add(r[7]);
                }
            }
        } catch (Exception ex) {
            return "Total: " + total + " | Could not read saved bookings: " + ex.getMessage();
        }
        int reserved = Math.min(total, reservedUnits.size());
        return "Total units: " + total + "  |  Units with reservations: " + reserved
                + "  |  Units with no saved reservations: " + (total - reserved);
    }

    private void showOccupancy(BookableItem item) {
        StringBuilder out = new StringBuilder(item.getName() + "\nTotal units: " + capacityFor(item) + "\n\n");
        try {
            List<String[]> records = readInventoryRecords();
            for (int n = 1; n <= capacityFor(item); n++) {
                String unit = ((item instanceof ConferenceVenue) ? "CR-" : (item instanceof BallroomVenue) ? "BR-" : (item instanceof GymVenue) ? "GY-" : roomPrefix(item)) + String.format("%02d", n);
                List<String> details = new ArrayList<>();
                for (String[] r : records) {
                    if (r.length > 7 && r[2].equals(item.getName()) && r[7].equalsIgnoreCase(unit)) {
                        String time = (item instanceof Venue || item instanceof GymVenue) ? ", " + r[5] + "-" + r[6] : "";
                        details.add("Reserved — " + r[0] + " (" + r[3] + " to " + r[4] + time + ")");
                    }
                }
                out.append(unit).append(" — ").append(details.isEmpty() ? "Available (no saved reservation)" : String.join("; ", details)).append("\n");
            }
        } catch (Exception ex) { out.append("Could not read inventory: ").append(ex.getMessage()); }
        JTextArea area = new JTextArea(out.toString(), 22, 60);
        area.setEditable(false); area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), item.getName() + " Occupancy", JOptionPane.INFORMATION_MESSAGE);
    }
    private void updateInventoryPageIfOpen() {
        if (tabs != null) { tabs.setComponentAt(5, buildInventoryPage()); tabs.revalidate(); tabs.repaint(); }
    }

    private JPanel buildReviewsPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("GUEST STORIES & STAR RATINGS", "Every stay has a story. Explore guest feedback from ZATAM Grand Luxury Hotel.");
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CREAM);
        list.setBorder(new EmptyBorder(8, 8, 8, 8));
        File file = new File(REVIEWS_FILE);
        boolean any = false;
        if (file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 3);
                    if (parts.length != 3) continue;
                    any = true;
                    double value = 0;
                    try { value = Double.parseDouble(parts[1]); } catch (NumberFormatException ignored) { }
                    String stars = renderStars(value);
                    JPanel card = new JPanel(new BorderLayout(8, 8));
                    card.setBackground(Color.WHITE);
                    card.setBorder(new CompoundBorder(new LineBorder(new Color(222, 205, 166), 1), new EmptyBorder(16, 18, 16, 18)));
                    JLabel guest = new JLabel("♛  " + parts[0]);
                    guest.setFont(new Font("Serif", Font.BOLD, 19)); guest.setForeground(NAVY);
                    JLabel rating = new JLabel(stars.replace("</html>", "   " + String.format(java.util.Locale.US, "%.1f", value) + " / 5</html>"));
                    rating.setFont(new Font("SansSerif", Font.BOLD, 17)); rating.setForeground(new Color(176, 119, 20));
                    JTextArea comment = new JTextArea(parts[2]); comment.setEditable(false); comment.setLineWrap(true); comment.setWrapStyleWord(true);
                    comment.setFont(new Font("SansSerif", Font.PLAIN, 15)); comment.setForeground(TEXT); comment.setBackground(Color.WHITE);
                    card.add(guest, BorderLayout.NORTH); card.add(comment, BorderLayout.CENTER); card.add(rating, BorderLayout.SOUTH);
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
                    list.add(card); list.add(Box.createVerticalStrut(12));
                }
            } catch (IOException ex) {
                list.add(new JLabel("Reviews could not be loaded: " + ex.getMessage()));
            }
        }
        if (!any) {
            JLabel empty = new JLabel("✧  NO REVIEWS YET — BE THE FIRST TO SHARE YOUR ZATAM EXPERIENCE  ✧", SwingConstants.CENTER);
            empty.setFont(new Font("Serif", Font.BOLD, 17)); empty.setForeground(NAVY); empty.setBorder(new EmptyBorder(40, 20, 40, 20));
            list.add(empty);
        }
        JScrollPane scroll = new JScrollPane(list); scroll.getVerticalScrollBar().setUnitIncrement(16);
        JButton addReview = goldButton("✦  WRITE A REVIEW");
        addReview.addActionListener(e -> addVisitorReview());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setOpaque(false); bottom.add(addReview);
        page.add(heading, BorderLayout.NORTH); page.add(scroll, BorderLayout.CENTER); page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private String renderStars(double rating) {
        int full = (int)Math.floor(rating);
        boolean half = rating - full >= 0.5;
        int empty = 5 - full - (half ? 1 : 0);
        StringBuilder stars = new StringBuilder("<html><span style='color:#DEB153'>");
        for (int i = 0; i < full; i++) stars.append("★");
        if (half) stars.append("⯪");
        stars.append("</span><span style='color:#C9C2B4'>");
        for (int i = 0; i < empty; i++) stars.append("☆");
        stars.append("</span></html>");
        return stars.toString();
    }

    private JPanel buildStaffPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading(
                "Hotel Staff Portal",
                "Log in to view room and venue categories, check guest records, and cancel individual reservations."
        );

        JPanel login = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        login.setBackground(Color.WHITE);
        JPasswordField staffId = new JPasswordField(22);
        JButton verify = darkButton("Login");
        JLabel status = new JLabel("Staff login required.");
        status.setForeground(NAVY);
        login.add(new JLabel("Staff ID:"));
        login.add(staffId);
        login.add(verify);
        login.add(status);

        // The guest records are displayed in a large scrollable area.
        JPanel guestPanel = new JPanel();
        guestPanel.setBackground(CREAM);
        guestPanel.setLayout(new BoxLayout(guestPanel, BoxLayout.Y_AXIS));
        guestPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane guestScroll = new JScrollPane(guestPanel);
        guestScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        guestScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        guestScroll.getVerticalScrollBar().setUnitIncrement(16);

        JTextArea dashboardSummary = new JTextArea();
        dashboardSummary.setEditable(false);
        dashboardSummary.setLineWrap(true);
        dashboardSummary.setWrapStyleWord(true);
        dashboardSummary.setFont(new Font("Monospaced", Font.PLAIN, 13));
        dashboardSummary.setBackground(new Color(249, 245, 235));
        dashboardSummary.setBorder(new EmptyBorder(10, 12, 10, 12));

        Runnable refreshDashboard = () -> {
            try {
                List<String[]> all = readInventoryRecords();
                StringBuilder summary = new StringBuilder("INVENTORY OCCUPANCY SUMMARY\n\n");
                for (BookableItem it : bookableItems) {
                    java.util.Set<String> occupiedUnits = new java.util.HashSet<>();
                    for (String[] record : all) {
                        if (record.length > 7
                                && record[2].equals(it.getName())
                                && !record[7].isBlank()
                                && !record[7].equalsIgnoreCase("WAITING")
                                && !record[7].equalsIgnoreCase("CANCELLED")) {
                            occupiedUnits.add(record[7]);
                        }
                    }
                    int total = capacityFor(it);
                    int reserved = Math.min(total, occupiedUnits.size());
                    String label = (it instanceof GymVenue) ? "Gym & Fitness Center" : it.getName();
                    summary.append(label)
                            .append(" | Total: ").append(total)
                            .append(" | Reserved: ").append(reserved)
                            .append(" | Available: ").append(Math.max(0, total - reserved))
                            .append('\n');
                }
                dashboardSummary.setText(summary.toString());
                dashboardSummary.setCaretPosition(0);
            } catch (Exception ex) {
                dashboardSummary.setText("Could not read inventory: " + ex.getMessage());
            }
        };

        CardLayout cardLayout = new CardLayout();
        JPanel staffCards = new JPanel(cardLayout);
        staffCards.setVisible(false);

        // DASHBOARD: summary, every room/venue category button, and Logout in the top-right corner.
        JPanel dashboardPage = new JPanel(new BorderLayout(10, 10));
        dashboardPage.setBackground(CREAM);
        dashboardPage.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel dashboardTitle = new JLabel("Staff Dashboard — Select a Category");
        dashboardTitle.setFont(new Font("Serif", Font.BOLD, 22));
        dashboardTitle.setForeground(NAVY);
        JButton logoutButton = darkButton("Logout");
        JPanel dashboardTop = new JPanel(new BorderLayout(8, 8));
        dashboardTop.setBackground(CREAM);
        dashboardTop.add(dashboardTitle, BorderLayout.CENTER);
        dashboardTop.add(logoutButton, BorderLayout.EAST);

        JScrollPane summaryScroll = new JScrollPane(dashboardSummary);
        summaryScroll.setPreferredSize(new Dimension(650, 150));
        summaryScroll.setMinimumSize(new Dimension(250, 110));
        summaryScroll.getVerticalScrollBar().setUnitIncrement(12);

        JLabel instruction = new JLabel("Choose any room type or facility below to view its guest records.");
        instruction.setFont(new Font("SansSerif", Font.BOLD, 14));
        instruction.setForeground(NAVY);
        instruction.setBorder(new EmptyBorder(12, 4, 6, 4));

        // Two-column grid with gaps prevents category buttons from touching or overlapping.
        GridLayout categoryGrid = new GridLayout(0, 2, 16, 16);
        JPanel categoryPanel = new JPanel(categoryGrid);
        categoryPanel.setBackground(CREAM);
        categoryPanel.setBorder(new EmptyBorder(12, 8, 16, 8));
        categoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        categoryPanel.setVisible(false);
        // One column on narrow layouts, two columns when enough width is available.
        categoryPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int columns = categoryPanel.getWidth() < 560 ? 1 : 2;
                if (categoryGrid.getColumns() != columns) {
                    categoryGrid.setColumns(columns);
                    categoryPanel.revalidate();
                }
            }
        });

        // GUEST PAGE: Back to Dashboard is always available above the scrollable guest records.
        JPanel guestPage = new JPanel(new BorderLayout(8, 8));
        guestPage.setBackground(CREAM);
        guestPage.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel guestTop = new JPanel(new BorderLayout(10, 8));
        guestTop.setBackground(CREAM);
        JButton backButton = darkButton("← Back to Dashboard");
        JLabel guestHeading = new JLabel("Guest Records");
        guestHeading.setFont(new Font("Serif", Font.BOLD, 22));
        guestHeading.setForeground(NAVY);
        guestTop.add(backButton, BorderLayout.WEST);
        guestTop.add(guestHeading, BorderLayout.CENTER);
        guestPage.add(guestTop, BorderLayout.NORTH);
        guestPage.add(guestScroll, BorderLayout.CENTER);

        // Each button opens guest data for that exact category.
        for (BookableItem item : bookableItems) {
            String buttonLabel = (item instanceof GymVenue) ? "Gym & Fitness Center" : item.getName();
            JButton categoryButton = darkButton(buttonLabel);
            categoryButton.setPreferredSize(new Dimension(230, 58));
            categoryButton.setMinimumSize(new Dimension(150, 48));
            categoryButton.setMargin(new Insets(10, 12, 10, 12));
            categoryButton.setToolTipText("View guest records for " + buttonLabel);
            categoryButton.addActionListener(ev -> {
                showStaffGuestsForItem(item, guestPanel);
                cardLayout.show(staffCards, "GUESTS");
            });
            categoryPanel.add(categoryButton);
        }
        JButton gymEnrollmentsButton = darkButton("Public Gym Enrollments — View Members");
        gymEnrollmentsButton.setPreferredSize(new Dimension(230, 58));
        gymEnrollmentsButton.setMinimumSize(new Dimension(150, 48));
        gymEnrollmentsButton.setMargin(new Insets(10, 12, 10, 12));
        gymEnrollmentsButton.setToolTipText("View all public gym members and their selected time slots");
        gymEnrollmentsButton.addActionListener(ev -> showPublicGymEnrollments(true));
        categoryPanel.add(gymEnrollmentsButton);

        JPanel dashboardBody = new JPanel();
        dashboardBody.setLayout(new BoxLayout(dashboardBody, BoxLayout.Y_AXIS));
        dashboardBody.setBackground(CREAM);
        summaryScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        instruction.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton seeReservations = goldButton("▣  SEE RESERVATIONS & ROOM CATEGORIES");
        seeReservations.setAlignmentX(Component.LEFT_ALIGNMENT);
        seeReservations.setFont(new Font("SansSerif", Font.BOLD, 14));
        seeReservations.addActionListener(e -> {
            boolean show = !categoryPanel.isVisible();
            categoryPanel.setVisible(show);
            seeReservations.setText(show ? "▣  HIDE RESERVATION CATEGORIES" : "▣  SEE RESERVATIONS & ROOM CATEGORIES");
            dashboardBody.revalidate(); dashboardBody.repaint();
        });
        dashboardBody.add(summaryScroll);
        dashboardBody.add(Box.createVerticalStrut(10));
        dashboardBody.add(seeReservations);
        dashboardBody.add(instruction);
        dashboardBody.add(categoryPanel);

        JScrollPane dashboardScroll = new JScrollPane(dashboardBody);
        dashboardScroll.setBorder(BorderFactory.createEmptyBorder());
        dashboardScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        dashboardScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(16);

        dashboardPage.add(dashboardTop, BorderLayout.NORTH);
        dashboardPage.add(dashboardScroll, BorderLayout.CENTER);

        staffCards.add(dashboardPage, "DASHBOARD");
        staffCards.add(guestPage, "GUESTS");

        backButton.addActionListener(e -> {
            refreshDashboard.run();
            cardLayout.show(staffCards, "DASHBOARD");
        });

        verify.addActionListener(e -> {
            if (!STAFF_ID.equals(new String(staffId.getPassword()))) {
                status.setText("Incorrect staff ID.");
                staffCards.setVisible(false);
                JOptionPane.showMessageDialog(this,
                        "Staff ID is incorrect. Guest data is protected.",
                        "Access denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            status.setText("Logged in successfully.");
            refreshDashboard.run();
            cardLayout.show(staffCards, "DASHBOARD");
            staffCards.setVisible(true);
            staffCards.revalidate();
            staffCards.repaint();
        });

        logoutButton.addActionListener(e -> {
            staffCards.setVisible(false);
            staffId.setText("");
            status.setText("Logged out. Staff login required.");
            guestPanel.removeAll();
            guestPanel.revalidate();
            guestPanel.repaint();
            cardLayout.show(staffCards, "DASHBOARD");
        });

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(false);
        content.add(login, BorderLayout.NORTH);
        content.add(staffCards, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private void showStaffGuestsForItem(BookableItem item, JPanel guestPanel) {
        guestPanel.removeAll();
        try {
            List<String[]> records = readInventoryRecords();
            boolean found = false;
            for (int i = 0; i < records.size(); i++) {
                String[] r = records.get(i);
                if (r.length < 10 || !r[2].equals(item.getName()) || r[7].equalsIgnoreCase("WAITING") || r[7].equalsIgnoreCase("CANCELLED")) continue;
                found = true;
                final String unit = r[7]; final String phone = r[1]; final String guestName = r[0];
                JPanel card = new JPanel(new BorderLayout(10,8)); card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(222,205,166)), new EmptyBorder(12,14,12,14)));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
                // Inventory records do not store the total, so calculate it from the saved dates/times and the item's rate.
                BookableItem bookedItem = null;
                for (BookableItem candidate : bookableItems) {
                    if (candidate.getName().equals(r[2])) { bookedItem = candidate; break; }
                }
                String totalText = "Not available";
                try {
                    if (bookedItem != null) {
                        long bookedDays = ChronoUnit.DAYS.between(LocalDate.parse(r[3]), LocalDate.parse(r[4]));
                        double bookingTotal;
                        if (bookedItem instanceof GymVenue) {
                            long bookedHours = java.time.Duration.between(LocalTime.parse(r[5]), LocalTime.parse(r[6])).toHours();
                            bookingTotal = Math.max(0, bookedHours) * bookedItem.getPricePerNight();
                        } else {
                            bookingTotal = Math.max(0, bookedDays) * bookedItem.getPricePerNight();
                        }
                        totalText = "PKR " + money(bookingTotal);
                    }
                } catch (Exception ignored) {
                    totalText = "Could not calculate";
                }
                boolean timedVenueBooking = bookedItem instanceof Venue || bookedItem instanceof GymVenue;
                String info = "Guest: " + r[0] + "\nPhone: " + r[1] + "\nItem: " + r[2] + "\nDates: " + r[3] + " to " + r[4]
                        + (timedVenueBooking ? "\nTime: " + r[5] + "–" + r[6] : "")
                        + "\nUnit: " + r[7] + "\nGuests: " + r[9]
                        + "\nTOTAL AMOUNT: " + totalText;
                JTextArea details = new JTextArea(info); details.setEditable(false); details.setBackground(Color.WHITE); details.setFont(new Font("SansSerif", Font.PLAIN, 14));
                JButton cancel = darkButton("Cancel This Reservation");
                cancel.setVisible(true);
                cancel.setMinimumSize(new Dimension(220, 42));
                cancel.setPreferredSize(new Dimension(240, 44));
                cancel.addActionListener(ev -> {
                    int answer = JOptionPane.showConfirmDialog(this, "Cancel reservation for " + guestName + " (" + unit + ")?", "Confirm cancellation", JOptionPane.YES_NO_OPTION);
                    if (answer != JOptionPane.YES_OPTION) return;
                    try {
                        List<String[]> current = readInventoryRecords(); boolean removed = false; List<String> keep = new ArrayList<>();
                        for (String[] row : current) {
                            if (!removed && row.length > 7 && row[7].equalsIgnoreCase(unit) && row[0].equals(guestName) && row[1].equals(phone) && row[2].equals(item.getName()) && row[3].equals(r[3]) && row[4].equals(r[4])) { removed = true; continue; }
                            keep.add(String.join("|", row));
                        }
                        if (!removed) { JOptionPane.showMessageDialog(this, "Reservation was not found; refresh the category list."); return; }
                        try (FileWriter w = new FileWriter(INVENTORY_FILE, false)) { for (String line : keep) w.write(line + System.lineSeparator()); }
                        JOptionPane.showMessageDialog(this, "Reservation cancelled. Inventory updated.");
                        showStaffGuestsForItem(item, guestPanel); updateInventoryPageIfOpen();
                    } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Could not cancel reservation: " + ex.getMessage()); }
                });
                card.add(details, BorderLayout.CENTER); card.add(cancel, BorderLayout.SOUTH); guestPanel.add(card); guestPanel.add(Box.createVerticalStrut(8));
            }
            if (!found) guestPanel.add(new JLabel("No saved reservations for " + item.getName() + "."));
        } catch (Exception ex) { guestPanel.add(new JLabel("Could not read guest records: " + ex.getMessage())); }
        guestPanel.revalidate(); guestPanel.repaint();
    }

    private JPanel buildContactPage() {
        JPanel page = pagePanel();
        JPanel heading = pageHeading("ROYALDESK | GUEST RELATIONS", "Your dedicated connection to ZATAM Grand Luxury Hotel — reservations, events and guest care.");
        JPanel details = new JPanel(new GridLayout(0, 2, 16, 16));
        details.setBackground(CREAM);
        details.setBorder(new EmptyBorder(18, 8, 18, 8));
        String[][] cards = {
                {"♛  RESERVATIONS & FRONT DESK", "+92 300 1234567", "Stay planning, booking enquiries and guest assistance"},
                {"✦  EVENTS & CONFERENCES", "Events Concierge", "Conference rooms, grand ballrooms and private events"},
                {"◈  FITNESS & MEMBERSHIP", "Wellness Concierge", "Public gym membership and private gym sessions"},
                {"⌖  VISIT ZATAM", "Karachi, Pakistan", "ZATAM Grand Luxury Hotel • Four Queens"},
                {"◷  GUEST SUPPORT", "24 HOURS", "Front desk assistance for hotel guests"},
                {"✧  ROYALDESK", "Guest Experience System", "Reservations • Facilities • Guest Reviews"}
        };
        for (String[] item : cards) {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new LineBorder(GOLD, 1), new EmptyBorder(18, 18, 18, 18)));
            JLabel title = new JLabel(item[0]); title.setForeground(new Color(132, 86, 21));
            title.setFont(new Font("Serif", Font.BOLD, 17));
            JLabel value = new JLabel("<html><b>" + item[1] + "</b><br><br>" + item[2] + "</html>");
            value.setForeground(TEXT); value.setFont(new Font("SansSerif", Font.PLAIN, 14));
            card.add(title, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); details.add(card);
        }
        JScrollPane scroll = new JScrollPane(details); scroll.setBorder(BorderFactory.createEmptyBorder());
        page.add(heading, BorderLayout.NORTH); page.add(scroll, BorderLayout.CENTER); return page;
    }

    private void addVisitorReview() {
        JTextField name = new JTextField(22);
        JComboBox<Double> rating = new JComboBox<>(new Double[]{5.0,4.5,4.0,3.5,3.0,2.5,2.0,1.5,1.0,0.5});
        JTextArea comment = new JTextArea(5, 25); comment.setLineWrap(true); comment.setWrapStyleWord(true);
        JPanel form = new JPanel(new GridBagLayout()); GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(6,6,6,6); g.anchor=GridBagConstraints.WEST; g.fill=GridBagConstraints.HORIZONTAL;
        addFormRow(form,g,0,"Your name:",name); addFormRow(form,g,1,"Rating (0.5–5 stars):",rating); addFormRow(form,g,2,"Your review:",new JScrollPane(comment));
        if(JOptionPane.showConfirmDialog(this,form,"Share Your ZATAM Review",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
        if(name.getText().trim().isEmpty()||comment.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Please enter your name and review.");return;}
        String safeName=name.getText().trim().replace("|","/").replace("\n"," "); String safeComment=comment.getText().trim().replace("|","/").replace("\n"," ");
        try(FileWriter w=new FileWriter(REVIEWS_FILE,true)){w.write(safeName+"|"+rating.getSelectedItem()+"|"+safeComment+System.lineSeparator()); refreshReviewsPage(); JOptionPane.showMessageDialog(this,"Thank you! Your review has been saved.");}catch(IOException ex){JOptionPane.showMessageDialog(this,"Could not save review: "+ex.getMessage());}
    }

    private void enrollPublicGymMember() {
        JTextField name = new JTextField(22);
        JTextField phone = new JTextField(18);
        JTextField months = new JTextField("1", 6);
        JComboBox<String> timeSlot = new JComboBox<>(PUBLIC_GYM_TIME_SLOTS);
        JLabel capacityLabel = new JLabel();
        Runnable refreshCapacity = () -> {
            String slot = (String) timeSlot.getSelectedItem();
            int enrolled = countGymMembersForTimeSlot(slot);
            capacityLabel.setText("Selected slot: " + enrolled + " / " + PUBLIC_GYM_CAPACITY_PER_TIME_SLOT + " members enrolled");
        };
        timeSlot.addActionListener(e -> refreshCapacity.run());
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Member name:")); form.add(name);
        form.add(new JLabel("Phone number:")); form.add(phone);
        form.add(new JLabel("Membership months (1–24):")); form.add(months);
        form.add(new JLabel("Preferred gym time:")); form.add(timeSlot);
        form.add(new JLabel("Capacity (40 people per slot):")); form.add(capacityLabel);
        refreshCapacity.run();
        if (JOptionPane.showConfirmDialog(this, form, "Public Gym Membership Enrollment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            int m = Integer.parseInt(months.getText().trim());
            String selectedSlot = (String) timeSlot.getSelectedItem();
            if (name.getText().trim().isEmpty() || !phone.getText().trim().matches("[0-9+() -]{7,20}") || m < 1 || m > 24) {
                throw new IllegalArgumentException("Enter a name, valid phone number, and membership duration from 1 to 24 months.");
            }
            int enrolled = countGymMembersForTimeSlot(selectedSlot);
            if (enrolled >= PUBLIC_GYM_CAPACITY_PER_TIME_SLOT) {
                throw new IllegalArgumentException("This gym time slot is full (" + PUBLIC_GYM_CAPACITY_PER_TIME_SLOT + " people maximum). Please select another time.");
            }
            double rate = 10000.0;
            String id = "GYM-M-" + System.currentTimeMillis();
            String safeName = name.getText().trim().replace("|", "/").replace("\n", " ");
            String safePhone = phone.getText().trim().replace("|", "/");
            LocalDate enrollmentDate = LocalDate.now();
            LocalTime enrollmentTime = LocalTime.now().withSecond(0).withNano(0);
            LocalDate nextBillingDate = enrollmentDate.plusMonths(m);
            try (FileWriter w = new FileWriter(GYM_MEMBERS_FILE, true)) {
                w.write(id + "|" + safeName + "|" + safePhone + "|" + m + "|" + (m * rate) + "|" + selectedSlot
                        + "|" + enrollmentDate + "|" + enrollmentTime + "|" + nextBillingDate + System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Public gym enrollment saved!\nMember ID: " + id + "\nMembership: " + m + " month(s)\nPreferred time: " + selectedSlot
                    + "\nEnrollment date: " + enrollmentDate + "\nEnrollment time: " + enrollmentTime
                    + "\nNext renewal/payment due: " + nextBillingDate + "\nSlot capacity: " + (enrolled + 1) + "/" + PUBLIC_GYM_CAPACITY_PER_TIME_SLOT
                    + " people\nExample total: PKR " + money(m * rate) + "\nSaved to " + GYM_MEMBERS_FILE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Membership months must be a number from 1 to 24.", "Invalid membership", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not enroll: " + ex.getMessage(), "Enrollment error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int countGymMembersForTimeSlot(String selectedSlot) {
        if (selectedSlot == null) return 0;
        int count = 0;
        File file = new File(GYM_MEMBERS_FILE);
        if (!file.exists()) return 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\|", -1);
                if (fields.length >= 6 && fields[5].equals(selectedSlot)) count++;
            }
        } catch (IOException ex) {
            System.err.println("Could not count public gym members: " + ex.getMessage());
        }
        return count;
    }

    private String safeNextBillingDate(String enrollmentDate, String months) {
        try { return LocalDate.parse(enrollmentDate).plusMonths(Integer.parseInt(months)).toString(); }
        catch (Exception ex) { return "Not available"; }
    }

    private void showPublicGymEnrollments(boolean staffView) {
        JDialog dialog = new JDialog(this, staffView ? "Staff — Public Gym Enrollments" : "Public Gym Memberships", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(900, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setBorder(new EmptyBorder(12, 14, 4, 14));
        top.setBackground(CREAM);
        JLabel heading = new JLabel(staffView ? "PUBLIC GYM ENROLLMENTS — STAFF DETAILS" : "PUBLIC GYM MEMBERSHIPS");
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        heading.setForeground(NAVY);
        top.add(heading, BorderLayout.NORTH);
        JTextArea capacityArea = new JTextArea(4, 60);
        capacityArea.setEditable(false);
        capacityArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        capacityArea.setBackground(CREAM);
        top.add(capacityArea, BorderLayout.CENTER);
        dialog.add(top, BorderLayout.NORTH);

        JPanel memberList = new JPanel();
        memberList.setLayout(new BoxLayout(memberList, BoxLayout.Y_AXIS));
        memberList.setBackground(CREAM);
        memberList.setBorder(new EmptyBorder(8, 12, 8, 12));
        JScrollPane scroll = new JScrollPane(memberList);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scroll, BorderLayout.CENTER);

        Runnable[] refresh = new Runnable[1];
        refresh[0] = () -> {
            StringBuilder capacity = new StringBuilder("Maximum capacity: ")
                    .append(PUBLIC_GYM_CAPACITY_PER_TIME_SLOT).append(" people per time slot\n");
            for (String slot : PUBLIC_GYM_TIME_SLOTS) {
                capacity.append(slot).append(" — ").append(countGymMembersForTimeSlot(slot)).append("/")
                        .append(PUBLIC_GYM_CAPACITY_PER_TIME_SLOT).append(" enrolled\n");
            }
            capacityArea.setText(capacity.toString());
            memberList.removeAll();
            File file = new File(GYM_MEMBERS_FILE);
            int total = 0;
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] fields = line.split("\\|", -1);
                        if (fields.length < 5) continue;
                        total++;
                        String id = fields[0];
                        String memberName = fields[1];
                        String memberPhone = fields[2];
                        String months = fields[3];
                        String amount = fields[4];
                        String slot = fields.length >= 6 && !fields[5].isBlank()
                                ? fields[5] : "Not selected (older record)";
                        String enrollmentDate = fields.length >= 7 && !fields[6].isBlank() ? fields[6] : "Unknown (older record)";
                        String enrollmentTime = fields.length >= 8 && !fields[7].isBlank() ? fields[7] : "Unknown (older record)";
                        String nextBillingDate = fields.length >= 9 && !fields[8].isBlank() ? fields[8]
                                : (fields.length >= 7 && !fields[6].isBlank() ? safeNextBillingDate(fields[6], months) : "Not available for older record");

                        JPanel row = new JPanel(new BorderLayout(12, 6));
                        row.setBackground(Color.WHITE);
                        row.setBorder(new CompoundBorder(new LineBorder(new Color(220, 210, 190)),
                                new EmptyBorder(10, 10, 10, 10)));
                        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                        String visibleText = staffView
                                ? total + ") " + memberName + " | Phone: " + memberPhone
                                  + " | ID: " + id + " | Membership: " + months + " month(s)"
                                  + " | Amount: PKR " + amount + " | Gym Slot: " + slot
                                  + " | Enrollment Date: " + enrollmentDate + " | Enrollment Time: " + enrollmentTime
                                  + " | Next Renewal/Payment Due: " + nextBillingDate
                                : total + ") " + memberName;
                        JTextArea details = new JTextArea(visibleText);
                        details.setEditable(false);
                        details.setLineWrap(true);
                        details.setWrapStyleWord(true);
                        details.setOpaque(false);
                        details.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        if (staffView) {
                            JButton cancel = new JButton("Cancel Enrollment");
                            cancel.setUI(new javax.swing.plaf.basic.BasicButtonUI());
                            cancel.setForeground(Color.WHITE);
                            cancel.setBackground(new Color(150, 45, 40));
                            cancel.setOpaque(true);
                            cancel.setContentAreaFilled(true);
                            cancel.setBorderPainted(true);
                            cancel.setBorder(new CompoundBorder(new LineBorder(new Color(218, 170, 65), 2),
                                    new EmptyBorder(9, 12, 9, 12)));
                            cancel.setFont(new Font("SansSerif", Font.BOLD, 13));
                            cancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            cancel.setPreferredSize(new Dimension(165, 42));
                            cancel.setMinimumSize(new Dimension(165, 42));
                            cancel.setFocusPainted(false);
                            cancel.addActionListener(e -> {
                                int choice = JOptionPane.showConfirmDialog(dialog,
                                        "Cancel public gym enrollment for " + memberName + " (" + id + ")?",
                                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (choice == JOptionPane.YES_OPTION) {
                                    if (removeGymMemberById(id)) {
                                        JOptionPane.showMessageDialog(dialog, "Gym enrollment cancelled successfully.\nThe time-slot capacity has been updated.");
                                        refresh[0].run();
                                    } else {
                                        JOptionPane.showMessageDialog(dialog, "Could not cancel this enrollment. Please check " + GYM_MEMBERS_FILE + ".",
                                                "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            row.add(cancel, BorderLayout.EAST);
                        }
                        row.add(details, BorderLayout.CENTER);
                        row.setAlignmentX(Component.LEFT_ALIGNMENT);
                        memberList.add(row);
                        memberList.add(Box.createVerticalStrut(7));
                    }
                } catch (IOException ex) {
                    memberList.add(new JLabel("Could not read member records: " + ex.getMessage()));
                }
            }
            if (total == 0) {
                JLabel empty = new JLabel("No public gym members have enrolled yet.");
                empty.setBorder(new EmptyBorder(16, 8, 16, 8));
                memberList.add(empty);
            }
            memberList.revalidate();
            memberList.repaint();
        };
        refresh[0].run();

        JButton close = darkButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(CREAM);
        bottom.setBorder(new EmptyBorder(0, 10, 8, 10));
        bottom.add(close);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean removeGymMemberById(String memberId) {
        File file = new File(GYM_MEMBERS_FILE);
        if (!file.exists()) return false;
        List<String> keptLines = new ArrayList<>();
        boolean removed = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\|", -1);
                if (!removed && fields.length > 0 && fields[0].equals(memberId)) {
                    removed = true;
                } else {
                    keptLines.add(line);
                }
            }
        } catch (IOException ex) {
            return false;
        }
        if (!removed) return false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : keptLines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private String stylishPhotoCaption(String galleryTitle, String path) {
        String file = new File(path).getName().toLowerCase().replaceAll("\\.[^.]+$", "").replace('_', ' ').replace('-', ' ');
        String label;
        if (file.contains("washroom") || file.contains("restroom") || file.contains("bath")) label = "LUXURY WASHROOM";
        else if (file.contains("balcony")) label = "PRIVATE BALCONY VIEW";
        else if (file.contains("bed") || file.contains("bedroom") || file.contains("comfy")) label = "SIGNATURE BEDROOM";
        else if (file.contains("sitting") || file.contains("lounge")) label = "ELEGANT SITTING LOUNGE";
        else if (file.contains("night")) label = "ILLUMINATED NIGHT VIEW";
        else if (file.contains("day") || file.contains("morning")) label = "GRAND DAYTIME VIEW";
        else if (file.contains("pool")) label = "RESORT-STYLE POOL";
        else if (file.contains("dinning") || file.contains("dining")) label = "FINE DINING EXPERIENCE";
        else if (file.contains("gym")) label = "FITNESS & WELLNESS";
        else if (file.contains("lobby")) label = "GRAND LOBBY & RECEPTION";
        else if (file.contains("conference") || file.contains("audit")) label = "EXECUTIVE CONFERENCE SPACE";
        else if (file.contains("ball")) label = "ROYAL CELEBRATION BALLROOM";
        else if (file.contains("entrance")) label = "GRAND HOTEL ENTRANCE";
        else label = "SIGNATURE " + galleryTitle.toUpperCase();
        return "✦  " + label + "  ✦";
    }

    private String loadReviews(){StringBuilder b=new StringBuilder(); File f=new File(REVIEWS_FILE); if(!f.exists())return "No visitor reviews yet. Be the first to review ZATAM!"; try(BufferedReader r=new BufferedReader(new FileReader(f))){String line;while((line=r.readLine())!=null){String[] p=line.split("\\|",3); if(p.length==3)b.append(p[0]).append("  •  ").append(p[1]).append("/5 stars\n").append(p[2]).append("\n\n");}}catch(IOException ex){return "Could not load reviews: "+ex.getMessage();} return b.length()==0?"No visitor reviews yet.":b.toString();}
    private void refreshReviewsPage() { if(tabs!=null && tabs.getTabCount()>7) tabs.setComponentAt(7,buildReviewsPage()); }

    private void showGuestOwnBooking() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter the phone number used for your booking first."); return; }
        try {
            StringBuilder out = new StringBuilder();
            for (String[] r : readInventoryRecords()) if (r[1].equals(phone)) {
                out.append("Guest: ").append(r[0]).append("\nItem: ").append(r[2]).append("\nDates: ").append(r[3]).append(" to ").append(r[4]).append("\nUnit: ").append(r[7]).append("\nStatus: Confirmed\n\n");
            }
            if (out.length()==0) out.append("No reservation found for this phone number.");
            JTextArea area=new JTextArea(out.toString(),12,40); area.setEditable(false);
            JOptionPane.showMessageDialog(this,new JScrollPane(area),"Your Reservation Only",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Could not look up your reservation: "+ex.getMessage()); }
    }

    private void showSavedBookings() {
        StringBuilder result = new StringBuilder();
        FileReader reader = null;
        try {
            reader = new FileReader(RESERVATION_FILE);
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) result.append(buffer, 0, count);
            if (result.length() == 0) result.append("No reservations saved yet.");
            JTextArea area = new JTextArea(result.toString(), 20, 65);
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Saved Reservations", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "No saved reservations found yet.", "Reservations", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not read reservations: " + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (reader != null) {
                try { reader.close(); }
                catch (IOException ex) { System.err.println("Could not close file: " + ex.getMessage()); }
            }
        }
    }

    private void copyFileUsingStreams(String source, String destination) throws IOException {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        } finally {
            if (input != null) input.close();
            if (output != null) output.close();
        }
    }

    private void updateEstimate() {
        BookableItem item = getSelectedItem();
        try {
            LocalDate start = selectedDate(checkInField);
            LocalDate end = selectedDate(checkOutField);
            long days = ChronoUnit.DAYS.between(start, end);
            long hours = java.time.Duration.between(
                    LocalTime.parse((String) startTimeChoice.getSelectedItem()),
                    LocalTime.parse((String) endTimeChoice.getSelectedItem())).toHours();
            String dateLine = days > 0 ? "Selected dates: " + start + " to " + end + "<br>Days/nights: " + days + "<br>" : "Choose a checkout date after check-in.<br>";
            String timeLine = (item instanceof Venue || item instanceof GymVenue)
                    ? "Selected time: " + startTimeChoice.getSelectedItem() + "–" + endTimeChoice.getSelectedItem() + "<br>" : "";
            String totalLine = "";
            if (days > 0 && (!(item instanceof Venue || item instanceof GymVenue) || hours > 0)) {
                double total = item instanceof GymVenue ? Math.max(0, hours) * item.getPricePerNight() : days * item.getPricePerNight();
                totalLine = "<br><span style='color:#845615;font-size:16px'><b>Estimated total: PKR " + money(total) + "</b></span>";
            }
            estimateLabel.setText("<html><div style='width:255px;color:#302B24'>"
                    + "<h3 style='color:#11100F'>" + item.getName() + "</h3>"
                    + item.getDescription().replace("\n", "<br>") + "<br><br>"
                    + "Rate: PKR " + money(item.getPricePerNight()) + (item instanceof GymVenue ? " / hour" : item instanceof Venue ? " / event-day" : " / night") + "<br>"
                    + dateLine + timeLine + "Units total: " + capacityFor(item) + totalLine
                    + "<br><br>Taxes and optional services are not included.</div></html>");
        } catch (Exception ex) {
            estimateLabel.setText("<html><div style='width:255px;color:#302B24'><h3 style='color:#11100F'>" + item.getName() + "</h3>"
                    + item.getDescription().replace("\n", "<br>") + "<br><br>Rate: PKR " + money(item.getPricePerNight())
                    + (item instanceof GymVenue ? " / hour" : item instanceof Venue ? " / event-day" : " / night")
                    + "<br><br>Select dates to calculate the estimated total.</div></html>");
        }
    }

    private static JSpinner createDatePicker(LocalDate initialDate) {
        SpinnerDateModel model = new SpinnerDateModel(toDate(initialDate), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner picker = new JSpinner(model);
        picker.setEditor(new JSpinner.DateEditor(picker, "yyyy-MM-dd"));
        ((JSpinner.DefaultEditor) picker.getEditor()).getTextField().setEditable(false);
        picker.setToolTipText("Use the arrow buttons to choose a date; typing is disabled.");
        picker.setPreferredSize(new Dimension(170, 28));
        return picker;
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate selectedDate(JSpinner picker) {
        return ((Date) picker.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void adjustOneDayCheckout() {
        BookableItem item = getSelectedItem();
        if (item instanceof ConferenceVenue || item instanceof BallroomVenue || item instanceof GymVenue) {
            LocalDate start = selectedDate(checkInField);
            if (!selectedDate(checkOutField).equals(start.plusDays(1))) {
                checkOutField.setValue(toDate(start.plusDays(1)));
            }
        } else if (!selectedDate(checkOutField).isAfter(selectedDate(checkInField))) {
            checkOutField.setValue(toDate(selectedDate(checkInField).plusDays(1)));
        }
    }

    private BookableItem getSelectedItem() {
        String selected = (String) itemChoice.getSelectedItem();
        for (BookableItem item : bookableItems) if (item.getName().equals(selected)) return item;
        return bookableItems.get(0);
    }

    private void addFormRow(JPanel panel, GridBagConstraints g, int row, String label, Component input) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0.42;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(TEXT);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(labelComponent, g);
        g.gridx = 1;
        g.weightx = 0.58;
        if (input instanceof JComponent) ((JComponent) input).setPreferredSize(new Dimension(260, 31));
        panel.add(input, g);
    }

    private JPanel pagePanel() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(CREAM);
        page.setBorder(new EmptyBorder(18, 22, 18, 22));
        return page;
    }

    private JPanel pageHeading(String title, String subtitle) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 3, 4));
        panel.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(new Font("Serif", Font.BOLD, 27));
        h.setForeground(NAVY);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("SansSerif", Font.PLAIN, 13));
        s.setForeground(TEXT);
        panel.add(h);
        panel.add(s);
        return panel;
    }

    // Scale and crop a photo to fill the whole display area, avoiding empty margins around it.
    private ImageIcon loadImageCover(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (!file.isFile()) return null;
        ImageIcon original = new ImageIcon(path);
        int ow = original.getIconWidth(), oh = original.getIconHeight();
        if (ow <= 0 || oh <= 0 || width <= 0 || height <= 0) return null;
        double scale = Math.max((double) width / ow, (double) height / oh);
        int sw = Math.max(width, (int) Math.ceil(ow * scale));
        int sh = Math.max(height, (int) Math.ceil(oh * scale));
        Image scaled = original.getImage().getScaledInstance(sw, sh, Image.SCALE_SMOOTH);
        java.awt.image.BufferedImage cropped = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = cropped.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(scaled, (width - sw) / 2, (height - sh) / 2, null);
        g.dispose();
        return new ImageIcon(cropped);
    }

    private ImageIcon loadScaledImage(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (!file.isFile()) return null;
        ImageIcon original = new ImageIcon(path);
        if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) return null;
        // Preserve the image's original proportions; never stretch it to a fixed rectangle.
        double scale = Math.min((double) width / original.getIconWidth(),
                (double) height / original.getIconHeight());
        int scaledWidth = Math.max(1, (int) Math.round(original.getIconWidth() * scale));
        int scaledHeight = Math.max(1, (int) Math.round(original.getIconHeight() * scale));
        Image scaled = original.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon loadPhotoFit(String path, int maxWidth, int maxHeight) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (!file.isFile()) return null;
        ImageIcon original = new ImageIcon(path);
        int ow = original.getIconWidth(), oh = original.getIconHeight();
        if (ow <= 0 || oh <= 0) return null;
        // Do not enlarge small source images: upscaling makes hotel photos look blurry.
        double scale = Math.min(1.0, Math.min((double) maxWidth / ow, (double) maxHeight / oh));
        int w = Math.max(1, (int) Math.round(ow * scale));
        int h = Math.max(1, (int) Math.round(oh * scale));
        Image scaled = original.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void resetSelectedPageScroll() {
        if (tabs == null || tabs.getSelectedComponent() == null) return;
        resetScrollbars(tabs.getSelectedComponent());
    }

    private void resetScrollbars(Component component) {
        if (component instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) component;
            scroll.getVerticalScrollBar().setValue(0);
            scroll.getHorizontalScrollBar().setValue(0);
            scroll.getViewport().setViewPosition(new Point(0, 0));
            return;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) resetScrollbars(child);
        }
    }

    private JButton goldButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(GOLD);
        button.setForeground(NAVY);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(9, 12, 9, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton darkButton(String text) {
        JButton button = new JButton(text);
        // Use Swing's basic UI delegate so Windows themes cannot wash out the colors.
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        // Force a clearly visible dark button instead of relying on the OS Look & Feel.
        button.setBackground(new Color(18, 25, 43)); // deep navy
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(218, 170, 65), 2),
                new EmptyBorder(9, 14, 9, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setEnabled(true);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(38, 49, 72));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(18, 25, 43));
            }
        });
        return button;
    }

    private static String money(double amount) { return String.format("%,.0f", amount); }

    // ABSTRACT BASE CLASS: shared data and behavior for every bookable room/venue.
    public static abstract class BookableItem {
        private final String name;
        private final double pricePerNight;
        private final int maxGuests;
        private final String description;
        private final String[] imagePaths;

        public BookableItem(String name, double pricePerNight, int maxGuests, String description, String... imagePaths) {
            this.name = name;
            this.pricePerNight = pricePerNight;
            this.maxGuests = maxGuests;
            this.description = description;
            this.imagePaths = imagePaths;
        }
        public String getName() { return name; }
        public double getPricePerNight() { return pricePerNight; }
        public int getMaxGuests() { return maxGuests; }
        public String getDescription() { return description; }
        public String[] getImagePaths() { return imagePaths; }
    }

    public static abstract class Room extends BookableItem {
        public Room(String name, double price, int capacity, String description, String... photos) {
            super(name, price, capacity, description, photos);
        }
    }

    // Room prices are sample rates in Pakistani rupees for the project, not real hotel quotes.
    public static class StandardRoom extends Room {
        public StandardRoom() { super("Standard Room", 25000, 2,
                "A comfortable room for solo travellers or couples.\nBeds: one queen bed.\nFacilities: air conditioning, Wi-Fi, TV, work desk, private washroom.\nBalcony depends on the room assigned.",
                "pics/rooms/standard room/room view.png", "pics/rooms/standard room/comfy bed.png",
                "pics/rooms/standard room/balcony.png", "pics/rooms/standard room/washroom.png"); }
    }
    public static class DeluxeRoom extends Room {
        public DeluxeRoom() { super("Deluxe Room", 35000, 3,
                "An upgraded room with a generous sleeping area and sitting corner.\nBeds: king bed or twin setup (subject to availability).\nFacilities: private washroom, sitting area, air conditioning, Wi-Fi and TV.",
                "pics/rooms/deluxe room/deluxe room 1.jpg", "pics/rooms/deluxe room/deluxe bedroom 2.jpg",
                "pics/rooms/deluxe room/delux room sitting.jpg", "pics/rooms/deluxe room/deluxe washroom.jpg"); }
    }
    public static class ExecutiveRoom extends Room {
        public ExecutiveRoom() { super("Executive Room", 45000, 3,
                "A work-friendly premium room with additional comfort.\nBeds: king bed.\nFacilities: work desk, lounge chair, premium bedding, private washroom, Wi-Fi, TV and balcony.",
                "pics/rooms/executive room/executive room 1.jpg", "pics/rooms/executive room/executive room 2.jpg",
                "pics/rooms/executive room/executive washroom.jpg", "pics/rooms/executive room/executive balcony.jpg"); }
    }
    public static class FamilyRoom extends Room {
        public FamilyRoom() { super("Family Room", 60000, 4,
                "A spacious stay for families.\nBeds: family/twin/triple bed arrangements depending on selected layout.\nFacilities: family sleeping area, sitting space, private washroom and balcony in selected layouts.",
                "pics/rooms/family room/room.jpg", "pics/rooms/family room/twin bedroom.jpg",
                "pics/rooms/family room/twin bedroom 2.jpg", "pics/rooms/family room/family washroom.jpg",
                "pics/rooms/family room/family balcony.jpg", "pics/rooms/family room/triple bedroom .jpg",
                "pics/rooms/family room/4 beds room.jpg"); }
    }
    public static class JuniorSuite extends Room {
        public JuniorSuite() { super("Junior Suite", 75000, 3,
                "A suite-style room with extra space to relax.\nBeds: king bed plus a sitting area.\nFacilities: lounge corner, premium bedding, Wi-Fi, TV and private washroom.",
                "pics/suites/junior suite/junior suite 1.jpg", "pics/suites/junior suite/junior suite 2.jpg",
                "pics/suites/junior suite/junior site 3.jpg", "pics/suites/junior suite/junior suite washroom.jpg"); }
    }
    public static class PenthouseSuite extends Room {
        public PenthouseSuite() { super("Penthouse Suite", 130000, 4,
                "A premium suite for guests who want extra space and luxury.\nBeds: king bed plus lounge space.\nFacilities: luxury lounge, spacious bedroom, shower area, bathtub, Wi-Fi and TV.",
                "pics/suites/penthouse suite/penthouse suite 1.jpg", "pics/suites/penthouse suite/penthouse suite 2.jpg",
                "pics/suites/penthouse suite/penthouse shower area.jpg", "pics/suites/penthouse suite/penthouse bathtub.jpg"); }
    }
    public static class PresidentialSuite extends Room {
        public PresidentialSuite() { super("Presidential Suite", 200000, 4,
                "ZATAM's most exclusive suite, designed for a luxurious stay.\nBeds: king bed plus separate sitting area.\nFacilities: luxury bedroom, separate lounge, premium washroom, Wi-Fi, TV and room service.",
                "pics/suites/presidential suite/presidential suite(luxury).jpg", "pics/suites/presidential suite/presidential suite room.jpg",
                "pics/suites/presidential suite/presidential suite sitting(luxury).jpg", "pics/suites/presidential suite/presidential suite sitting.jpg",
                "pics/suites/presidential suite/presidential washroom.jpg", "pics/suites/presidential suite/presidential washroom(luxury).jpg"); }
    }

    public static abstract class Venue extends BookableItem {
        public Venue(String name, double pricePerEventDay, int maxGuests, String description, String... photos) {
            super(name, pricePerEventDay, maxGuests, description, photos);
        }
    }
    public static class ConferenceVenue extends Venue {
        public ConferenceVenue() { super("Conference Room", 150000, 150,
                "A professional venue for meetings, workshops and presentations.\nFacilities: meeting layout, presentation space and event seating.\nRate is a sample price per event-day.",
                "pics/conference rooms/room 1.jpg", "pics/conference rooms/room2.jpg", "pics/conference rooms/hall.jpg", "pics/conference rooms/• Auditórium.jpg"); }
    }
    public static class BallroomVenue extends Venue {
        public BallroomVenue() { super("Grand Ballroom", 400000, 600,
                "A grand event space for weddings, banquets and celebrations.\nFacilities: large hall, event seating and room for decoration/staging.\nRate is a sample price per event-day.",
                "pics/ball rooms/ball room1.jpg", "pics/ball rooms/ball room 2.jpg", "pics/ball rooms/ball room 3.jpg",
                "pics/ball rooms/ball room 4.jpg", "pics/ball rooms/ball room 5.jpg", "pics/ball rooms/ball room 6.jpg", "pics/ball rooms/ball room 7.jpg"); }
    }

    public static class GymVenue extends Venue {
        public GymVenue() { super("Private Gym Booking", 10000, 40,
                "Reserve one complete private gym for a timed session. Rate: PKR 10,000 per hour; booking blocks one of the two gyms.",
                "pics/GYM/gym.png", "pics/GYM/gymm.jpg"); }
    }

    // Encapsulation: booking data is private and exposed through getters.
    public static class Booking {
        private final String guestName, phone, itemName, checkIn, checkOut, paymentMethod;
        private final int guests;
        private final long days;
        private final double total;
        public Booking(String guestName, String phone, String itemName, String checkIn, String checkOut,
                       int guests, long days, double total, String paymentMethod) {
            this.guestName = guestName; this.phone = phone; this.itemName = itemName;
            this.checkIn = checkIn; this.checkOut = checkOut; this.guests = guests;
            this.days = days; this.total = total; this.paymentMethod = paymentMethod;
        }
        public String getGuestName() { return guestName; }
        public String getItemName() { return itemName; }
        public double getTotal() { return total; }
        public String toFileLine() {
            return "Contact=" + guestName + " | Phone=" + phone + " | Item=" + itemName
                    + " | Start=" + checkIn + " | End=" + checkOut + " | Guests=" + guests
                    + " | Days=" + days + " | Estimated total PKR=" + money(total) + " | Payment=" + paymentMethod;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ex) { System.err.println("Could not load system look and feel."); }
            new ZATAMHotel();
        });
    }
}
