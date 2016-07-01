package br.com.memorify.newsapp.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.memorify.newsapp.R;
import br.com.memorify.newsapp.model.Story;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context context;
    private List<Story> stories;
    private ItemClickListener listener;

    public interface ItemClickListener {
        void onItemClicked(Story story);
    }

    public StoryAdapter(Context context, List<Story> stories, ItemClickListener listener) {
        this.context = context;
        this.stories = stories;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(stories.get(position));
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View contentView;
        private ImageView thumbnailImageView;
        private TextView titleTextView;
        private TextView publicationDateTextView;
        private TextView trailTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            bindViews(itemView);
        }

        private void bindViews(View itemView) {
            contentView = itemView.findViewById(R.id.content_view);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.story_thumbnail);
            titleTextView = (TextView) itemView.findViewById(R.id.story_title);
            publicationDateTextView = (TextView) itemView.findViewById(R.id.story_publication_date);
            trailTextView = (TextView) itemView.findViewById(R.id.story_trail_text);
        }

        public void bind(final Story story) {
            titleTextView.setText(story.title);
            publicationDateTextView.setText(story.publicationDate);
            trailTextView.setText(Html.fromHtml(story.trailText));
            if (story.thumbnailURL == null || story.thumbnailURL.isEmpty()) {
                thumbnailImageView.setVisibility(View.GONE);
            } else {
                thumbnailImageView.setVisibility(View.VISIBLE);
                Glide.with(context).load(story.thumbnailURL)
                        .into(thumbnailImageView);
            }
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(story);
                    }
                }
            });
        }
    }
}
