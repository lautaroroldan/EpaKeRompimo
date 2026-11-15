package com.lautaroroldan.epakerompimo.listener;

import com.lautaroroldan.epakerompimo.dto.GameDTO;
import com.lautaroroldan.epakerompimo.entity.User;
import com.lautaroroldan.epakerompimo.enums.NotificationMessage;
import com.lautaroroldan.epakerompimo.enums.NotificationType;
import com.lautaroroldan.epakerompimo.event.FavoriteGameAddedEvent;
import com.lautaroroldan.epakerompimo.provider.gamecatalog.GameCatalogProvider;
import com.lautaroroldan.epakerompimo.repository.UserRepository;
import com.lautaroroldan.epakerompimo.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
public class FavoriteNotificationListener {

    private final UserRepository userRepository;
    private final List<GameCatalogProvider> gameCatalogProviders;
    private final NotificationService notificationService;

    public FavoriteNotificationListener(UserRepository userRepository,
                                        List<GameCatalogProvider> gameCatalogProviders,
                                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.gameCatalogProviders = gameCatalogProviders;
        this.notificationService = notificationService;
    }

    // Después del commit (el favorito ya está guardado) y en otro hilo (no demora la respuesta)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFavoriteGameAdded(FavoriteGameAddedEvent event) {
        User user = userRepository.findWithEmailById(event.idUser()).orElse(null);

        // Solo a mails confirmados: un mail pendiente todavía no cuenta
        if (user == null || user.getEmail() == null || user.getEmail().getAddress() == null) {
            return;
        }

        String title = findTitle(event.itadGameId());

        notificationService.send(
                user,
                user.getEmail().getAddress(),
                NotificationType.FAVORITE_GAME_ADDED,
                "Juego agregado a tus favoritos",
                List.of(NotificationMessage.FAVORITE_GAME_ADDED_MESSAGE),
                Map.of("game", title != null ? title : "Un juego"));
    }

    // Busca el título en el primer proveedor que conozca el juego
    private String findTitle(String itadGameId) {
        for (GameCatalogProvider provider : gameCatalogProviders) {
            GameDTO game = provider.findGameById(itadGameId);
            if (game != null && game.title() != null && !game.title().isBlank()) {
                return game.title();
            }
        }
        return null;
    }
}