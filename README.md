# Chess Engine with Neural Network Evaluation

A Java chess application featuring a fully functional chess engine paired with a neural network position evaluator. The engine plays at a **beginner level** — it understands the rules, avoids basic blunders, and can deliver checkmate, but will not challenge experienced players.

---

## Features

### Chess Implementation
- Complete rule set including castling, en passant, and pawn promotion
- Legal move generation via pseudo-legal move filtering
- Check, checkmate, and stalemate detection
- Flippable board perspective (play as white or black)
- Adjustable board size

### AI Engine
- **Minimax search** with alpha-beta pruning
- **Iterative deepening** with a configurable time limit — searches progressively deeper and always returns the best move found within the time budget
- **Transposition table** using Zobrist hashing to cache previously evaluated positions and avoid redundant search
- **MVV-LVA move ordering** (Most Valuable Victim, Least Valuable Attacker) to improve pruning efficiency by searching promising captures first

### Neural Network Evaluator
- Trained on grandmaster games parsed from PGN format
- Architecture: 781 inputs → 128 → 64 → 32 → 1 output (tanh activation)
- Input encoding: 12 binary piece-plane bitboards (6 piece types × 2 colors × 64 squares) plus castling rights, en passant file, and side to move
- Output: a score in the range [−1, 1] where positive values favor white and negative values favor black
- Weights exported from DL4J and evaluated at runtime via a lightweight manual forward pass — no ML framework required at inference time

---

## Architecture Overview

```
src/
├── game/
│   ├── Board.java              — board state, make/undo move, Zobrist hash
│   ├── GameManager.java        — turn management, player input, AI triggering
│   ├── Move.java               — move representation (standard, castle, en passant, promotion)
│   └── Position.java           — row/col coordinate wrapper
├── pieces/
│   ├── Piece.java          —   abstract base class
│   └── [Pawn, Knight, Bishop, Rook, Queen, King].java
├── ml/
│   ├── BoardEncoder.java       — converts board state to 781-element float array
│   ├── ChessEvaluator.java     — minimax search, alpha-beta, transposition table
│   ├── NNEvaluator.java        — lightweight runtime forward pass
│   ├── ModelTrainer.java       — DL4J training pipeline
    ├── StockfishEvaluator.java —
│   └── TrainingDataGen.java    — self-play sample generation
├── parser/
│   └── PGNParser.java          — parses PGN game files into training samples
├── transposition/
│   ├── TTEntry.java            — transposition table entry (score, depth, flag, best move)
│   └── ZobristHash.java        — Zobrist key generation and management
└── ui/
    ├── ChessPanel.java         — Swing board rendering and mouse input
    ├── StartWindow.java        — game configuration screen
    └── ImageStorage.java       — piece image caching
```

---

## How the AI Works

The AI uses **minimax search with alpha-beta pruning** to explore a tree of possible moves up to a configurable depth. At each leaf node, the position is evaluated by the neural network which returns a score reflecting who has the advantage.

**Iterative deepening** means the search runs at depth 1, then 2, then 3, and so on — stopping when the time limit is exceeded. Because earlier depths populate the transposition table, each deeper search benefits from cached results and improved move ordering, making the search significantly more efficient than jumping straight to the target depth.

---

## Training

The neural network was trained on a dataset of grandmaster PGN games using DL4J. Positions were encoded as 781-element binary feature vectors and labeled using the Stockfish position evaltuator which describes if white
or black has the advantage. Training used the Adam optimizer with early stopping based on validation loss.

For runtime performance, the trained weights are exported to a binary file and evaluated using a hand-written forward pass (matrix multiply + ReLU + tanh), removing all DL4J dependency from the game loop. A single evaluation takes roughly 0.01ms, making depth 4–5 search practical within a 3–5 second time budget.

---

## AI Strength

The engine plays at a **beginner level**. It will:
- Avoid hanging pieces in most positions
- Deliver basic checkmates (back rank, two rook, queen and king)
- Punish obvious blunders
- Develop pieces with some positional awareness

It will not:
- Consistently execute multi-move combinations
- Play strong endgames
- Match even a casual club player

Strength can be improved by increasing search depth or using a larger network trained on more data.

---

## Dependencies

| Library | Purpose |
|---|---|
| [DeepLearning4J (DL4J)](https://deeplearning4j.konduit.ai/) | Neural network training |
| [ND4J](https://github.com/eclipse/deeplearning4j/tree/master/nd4j) | Tensor operations during training |
| [ChessLib](https://github.com/bhlangonijr/chesslib) by **bhlangonijr** | PGN file parsing for training data generation |

### ChessLib Credit
PGN parsing is powered by [chesslib](https://github.com/bhlangonijr/chesslib), an open-source Java chess library developed by **bhlangonijr**. ChessLib handles algebraic notation parsing, move list extraction, and game result reading from standard PGN files. See the [chesslib license](https://github.com/bhlangonijr/chesslib/blob/master/LICENSE) for terms.

---

## Author

**Matthew Vanhoomissen**