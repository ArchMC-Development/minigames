#!/bin/bash
# convert-to-submodules-gh.sh

# Configuration
GITHUB_ORG="GrowlyX"  # Your organization
TEMP_DIR="/tmp/arch-submodules-$(date +%s)"
MAIN_REPO_PATH="/Users/subham/IdeaProjects/Organizations/ArchMC/arch-minigames"

# List of modules to convert
GAMES_TO_CONVERT=(
    "persistentgames/housing",
    "persistentgames/housing-api",
    "persistentgames/housing-lobby"
)

echo "Creating temp directory: $TEMP_DIR"
mkdir -p $TEMP_DIR

cd $MAIN_REPO_PATH

for game_path in "${GAMES_TO_CONVERT[@]}"; do
    game_name=$(basename $game_path)
    parent_dir=$(basename $(dirname $game_path))
    repo_name="${parent_dir}-${game_name}"  # e.g., microgames-events

    echo "================================================"
    echo "Processing: $game_path -> $repo_name"
    echo "================================================"

    # Check if directory exists
    if [ ! -d "$game_path" ]; then
        echo "⚠️  Directory $game_path doesn't exist, skipping..."
        continue
    fi

    # Copy to temp location
    echo "📁 Copying $game_path to temp location..."
    cp -r "$game_path" "$TEMP_DIR/$repo_name"

    # Initialize as git repo
    cd "$TEMP_DIR/$repo_name"
    git init
    git add .
    git commit -m "Initial commit for $repo_name"

    # Create GitHub repo using gh CLI
    echo "🌐 Creating GitHub repository: $GITHUB_ORG/$repo_name"
    gh repo create "$GITHUB_ORG/$repo_name" \
        --private \
        --description "ArchMC $repo_name module" \
        --source=. \
        --remote=origin \
        --push

    if [ $? -ne 0 ]; then
        echo "❌ Failed to create repo for $repo_name"
        continue
    fi

    # Go back to main repo
    cd "$MAIN_REPO_PATH"

    # Remove from main repo
    echo "🗑️  Removing $game_path from main repository..."
    git rm -r "$game_path"

    # Add as submodule
    echo "➕ Adding $game_path as submodule..."
    git submodule add "git@github.com:$GITHUB_ORG/$repo_name.git" "$game_path"

    echo "✅ Successfully converted $game_path to submodule"
    echo ""
done

# Commit all changes
echo "📝 Committing all changes..."
git add .
git commit -m "Convert minigames and microgames to submodules"

echo ""
echo "🎉 Conversion complete!"
echo ""
echo "📋 Next steps:"
echo "1. Review the changes: git status"
echo "2. Push to remote: git push"
echo "3. Team members should run: git submodule update --init --recursive"
