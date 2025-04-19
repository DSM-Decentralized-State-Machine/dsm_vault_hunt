package teste.lucasvegi.pokemongooffline.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teste.lucasvegi.pokemongooffline.Util.BancoDadosSingleton;
import teste.lucasvegi.pokemongooffline.Util.MyApp;
import teste.lucasvegi.pokemongooffline.Util.TimeUtil;

/**
 * Facade Controller for DSM integration
 */
public final class DSMControllerFacadeSingleton {
    private static final String TAG = "DSMController";
    private Usuario user;
    private static DSMControllerFacadeSingleton INSTANCE = new DSMControllerFacadeSingleton();
    
    private Map<String, DSMLimboVault> claimedVaults = new HashMap<>();
    
    private DSMControllerFacadeSingleton() {
        // Initialize controller
        createDSMTables();
    }
    
    /**
     * Create DSM-specific tables if they don't exist
     */
    private void createDSMTables() {
        try {
            // Create table for vaults
            String createVaultTable = "CREATE TABLE IF NOT EXISTS dsm_vaults (" +
                    "chestId TEXT PRIMARY KEY, " +
                    "userId TEXT NOT NULL, " +
                    "regionId TEXT NOT NULL, " +
                    "variant TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "tokenAmount INTEGER NOT NULL, " +
                    "latitude REAL NOT NULL, " +
                    "longitude REAL NOT NULL, " +
                    "claimedAt INTEGER, " +
                    "FOREIGN KEY (userId) REFERENCES usuario(login))";
            
            BancoDadosSingleton.getInstance().executarSQL(createVaultTable);
            
            Log.d(TAG, "DSM tables created or already exist");
        } catch (Exception e) {
            Log.e(TAG, "Error creating DSM tables: " + e.getMessage());
        }
    }
    
    public static DSMControllerFacadeSingleton getInstance() {
        return INSTANCE;
    }
    
    public Usuario getUsuario() {
        return this.user;
    }
    
    private void daoUsuario() {
        Cursor c = BancoDadosSingleton.getInstance().buscar("usuario",
                new String[]{"login", "senha", "nome", "sexo", "foto", "dtCadastro", "xp", "nivel"},
                "", "");

        while (c.moveToNext()) {
            int login = c.getColumnIndex("login");
            int pass = c.getColumnIndex("senha");
            int name = c.getColumnIndex("nome");
            int sexo = c.getColumnIndex("sexo");
            int foto = c.getColumnIndex("foto");
            int dtCad = c.getColumnIndex("dtCadastro");
            int xp = c.getColumnIndex("xp");
            int nivel = c.getColumnIndex("nivel");

            user = new Usuario(c.getString(login));

            user.setSenha(c.getString(pass));
            user.setNome(c.getString(name));
            user.setSexo(c.getString(sexo));
            user.setFoto(c.getString(foto));
            user.setDtCadastro(c.getString(dtCad));
            user.setXp(c.getInt(xp));
            user.setNivel(c.getInt(nivel));
        }

        c.close();
    }
    
    /**
     * Add a claimed vault to the user's inventory
     */
    public void addClaimedVault(DSMLimboVault vault) {
        claimedVaults.put(vault.getChestId(), vault);
        
        // Store vault claim in database
        ContentValues values = new ContentValues();
        values.put("chestId", vault.getChestId());
        values.put("userId", user.getLogin());
        values.put("regionId", vault.getRegionId());
        values.put("variant", vault.getVariant().toString());
        values.put("status", vault.getStatus().toString());
        values.put("tokenAmount", vault.getTokenAmount());
        values.put("latitude", vault.getLatitude());
        values.put("longitude", vault.getLongitude());
        values.put("claimedAt", vault.getClaimedAt());
        
        BancoDadosSingleton.getInstance().inserir("dsm_vaults", values);
        
        // Add XP for the claim
        aumentaXp("vault_claim");
    }
    
    /**
     * Get all claimed vaults for the current user
     */
    public Map<String, DSMLimboVault> getClaimedVaults() {
        return claimedVaults;
    }
    
    /**
     * Record a vault withdrawal
     */
    public boolean withdrawVault(String chestId) {
        DSMLimboVault vault = claimedVaults.get(chestId);
        if (vault != null && vault.getStatus() == DSMLimboVault.ChestStatus.CLAIMED) {
            vault.withdraw();
            
            // Update status in database
            ContentValues values = new ContentValues();
            values.put("status", DSMLimboVault.ChestStatus.WITHDRAWN.toString());
            
            BancoDadosSingleton.getInstance().atualizar("dsm_vaults", values, 
                    "chestId = '" + chestId + "' AND userId = '" + user.getLogin() + "'");
            
            // Add XP for the withdrawal
            aumentaXp("vault_withdraw");
            return true;
        }
        return false;
    }
    
    public boolean loginUser(String login, String senha) {
        Cursor c = BancoDadosSingleton.getInstance().buscar("usuario",
                new String[]{"login", "senha", "temSessao"},
                "login = '" + login + "' AND senha = '" + senha + "'",
                "");

        if (c.getCount() == 1) {
            // Set user session
            ContentValues valores = new ContentValues();
            valores.put("temSessao", "SIM");
            BancoDadosSingleton.getInstance().atualizar("usuario", valores, "login = '" + login + "'");

            // Load user data
            daoUsuario();
            
            // Load claimed vaults
            loadClaimedVaults();

            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }
    
    /**
     * Load all claimed vaults for the current user from the database
     */
    private void loadClaimedVaults() {
        claimedVaults.clear();
        
        Cursor c = BancoDadosSingleton.getInstance().buscar("dsm_vaults",
                new String[]{"chestId", "regionId", "variant", "status", "tokenAmount", "latitude", "longitude", "claimedAt"},
                "userId = '" + user.getLogin() + "'", "");
        
        while (c.moveToNext()) {
            try {
                int chestIdIdx = c.getColumnIndex("chestId");
                int regionIdIdx = c.getColumnIndex("regionId");
                int variantIdx = c.getColumnIndex("variant");
                int statusIdx = c.getColumnIndex("status");
                int tokenAmountIdx = c.getColumnIndex("tokenAmount");
                int latitudeIdx = c.getColumnIndex("latitude");
                int longitudeIdx = c.getColumnIndex("longitude");
                int claimedAtIdx = c.getColumnIndex("claimedAt");
                
                String chestId = c.getString(chestIdIdx);
                String regionId = c.getString(regionIdIdx);
                DSMLimboVault.ChestVariant variant = DSMLimboVault.ChestVariant.valueOf(c.getString(variantIdx));
                DSMLimboVault.ChestStatus status = DSMLimboVault.ChestStatus.valueOf(c.getString(statusIdx));
                long tokenAmount = c.getLong(tokenAmountIdx);
                double latitude = c.getDouble(latitudeIdx);
                double longitude = c.getDouble(longitudeIdx);
                long claimedAt = c.getLong(claimedAtIdx);
                
                // Create vault
                DSMLimboVault vault = new DSMLimboVault(chestId, regionId, variant, latitude, longitude);
                
                // Set appropriate status
                if (status == DSMLimboVault.ChestStatus.CLAIMED) {
                    vault.claim(user.getLogin(), claimedAt);
                } else if (status == DSMLimboVault.ChestStatus.WITHDRAWN) {
                    vault.claim(user.getLogin(), claimedAt);
                    vault.withdraw();
                }
                
                claimedVaults.put(chestId, vault);
            } catch (Exception e) {
                Log.e(TAG, "Error loading vault: " + e.getMessage());
            }
        }
        
        c.close();
    }

    public boolean logoutUser() {
        // Close user session
        ContentValues valores = new ContentValues();
        valores.put("temSessao", "NAO");

        BancoDadosSingleton.getInstance().atualizar("usuario", valores, "login = '" + this.user.getLogin() + "'");
        return true;
    }

    public boolean cadastrarUser(String login, String senha, String nome, String sexo, String foto) {
        Map<String, String> timeStamp = TimeUtil.getHoraMinutoSegundoDiaMesAno();

        ContentValues valores = new ContentValues();
        valores.put("login", login);
        valores.put("senha", senha);
        valores.put("nome", nome);
        valores.put("sexo", sexo);
        valores.put("foto", foto);
        valores.put("dtCadastro", timeStamp.get("dia") + "/" + timeStamp.get("mes") + "/" + timeStamp.get("ano") + " " + timeStamp.get("hora") + ":" + timeStamp.get("minuto") + ":" + timeStamp.get("segundo"));
        valores.put("temSessao", "SIM");
        valores.put("xp", 0);
        valores.put("nivel", 1);

        // Only clear the current user data, not the Pokemon data
        BancoDadosSingleton.getInstance().deletar("usuario", "login = '" + login + "'");

        BancoDadosSingleton.getInstance().inserir("usuario", valores);

        // Load user data
        daoUsuario();
        return true;
    }

    public boolean temSessao() {
        Cursor sessao = BancoDadosSingleton.getInstance().buscar("usuario", new String[]{"login", "temSessao"}, "temSessao = 'SIM'", "");

        if (sessao.getCount() == 1) {
            // Load user data
            daoUsuario();
            // Load claimed vaults
            loadClaimedVaults();

            sessao.close();
            return true;
        } else {
            sessao.close();
            return false;
        }
    }

    public boolean aumentaXp(String evento) {
        final int xpRecebido = getXpEvento(evento);
        final int nivelAtual = getUsuario().getNivel();
        final int xpAtual = getUsuario().getXp();
        final int xpMax = xpMaximo(nivelAtual);
        int xpFinal = xpAtual, nivelFinal = nivelAtual;

        if ((xpAtual + xpRecebido) >= xpMax) {
            xpFinal = (xpAtual + xpRecebido) - xpMax;
            nivelFinal++;

            if (nivelFinal > 40) {
                nivelFinal = 40;
                xpFinal = xpMaximo(nivelFinal);
            }

            getUsuario().setNivel(nivelFinal);
        } else {
            xpFinal = xpAtual + xpRecebido;
        }

        getUsuario().setXp(xpFinal);

        ContentValues valores = new ContentValues();

        valores.put("login", getUsuario().getLogin());
        valores.put("senha", getUsuario().getSenha());
        valores.put("nome", getUsuario().getNome());
        valores.put("sexo", getUsuario().getSexo());
        valores.put("foto", getUsuario().getFoto());
        valores.put("dtCadastro", getUsuario().getDtCadastro());
        valores.put("temSessao", "SIM");
        valores.put("nivel", nivelFinal);
        valores.put("xp", xpFinal);

        int count = BancoDadosSingleton.getInstance().atualizar("usuario", valores, "login='" + getUsuario().getLogin() + "'");

        if (count == 1) {
            Toast.makeText(MyApp.getAppContext(), "You earned " + xpRecebido + " XP", Toast.LENGTH_SHORT).show();

            if (nivelFinal > nivelAtual) {
                Toast.makeText(MyApp.getAppContext(), "Congratulations! You advanced to level " + nivelFinal, Toast.LENGTH_SHORT).show();
            }
        }

        return count == 1;
    }

    public int xpMaximo(int nivelUsuario) {
        return nivelUsuario * 1000;
    }

    public int getXpEvento(String evento) {
        switch (evento) {
            case "vault_claim":
                return 100;
            case "vault_withdraw":
                return 500;
            default:
                return 0;
        }
    }
}
