import { LitElement, css, html, nothing } from 'lit';
import { api } from '../api.js';
import './todo-form.js';
import './todo-list.js';
import './todo-pager.js';

const FILTERS = ['all', 'active', 'completed'];

const EMPTY_MESSAGE = {
  all: 'Nothing here yet — add your first todo above.',
  active: 'No active todos. Nice.',
  completed: 'No completed todos yet.',
};

/**
 * Owns all state. Filtering, counting and paging are the server's job now, so
 * every mutation is followed by a reload of the current page rather than a
 * local edit of the list.
 */
export class TodoApp extends LitElement {
  static properties = {
    _items: { state: true },
    _page: { state: true },
    _totalPages: { state: true },
    _totalItems: { state: true },
    _activeCount: { state: true },
    _completedCount: { state: true },
    _filter: { state: true },
    _loading: { state: true },
    _error: { state: true },
  };

  /** Todos per page. */
  static PAGE_SIZE = 10;

  constructor() {
    super();
    this._items = [];
    this._page = 0;
    this._totalPages = 1;
    this._totalItems = 0;
    this._activeCount = 0;
    this._completedCount = 0;
    this._filter = 'all';
    this._loading = true;
    this._error = '';
  }

  connectedCallback() {
    super.connectedCallback();
    this.#load();
  }

  render() {
    return html`
      <main
        @todo-toggle=${(e) => this.#update(e.detail.id, { completed: e.detail.completed })}
        @todo-rename=${(e) => this.#update(e.detail.id, { title: e.detail.title })}
        @todo-delete=${(e) => this.#remove(e.detail.id)}
      >
        <h1>Todo</h1>

        <todo-form ?disabled=${this._loading} @todo-add=${(e) => this.#add(e.detail)}></todo-form>

        ${this._error
          ? html`<p class="error" role="alert">
              ${this._error}
              <button class="link" @click=${() => this.#load()}>Retry</button>
            </p>`
          : nothing}

        <section class="card">
          ${this._loading
            ? html`<p class="muted">Loading…</p>`
            : html`<todo-list
                .todos=${this._items}
                .emptyMessage=${EMPTY_MESSAGE[this._filter]}
              ></todo-list>`}
        </section>

        <todo-pager
          .page=${this._page}
          .totalPages=${this._totalPages}
          .totalItems=${this._totalItems}
          ?disabled=${this._loading}
          @page-change=${(e) => this.#load(e.detail)}
        ></todo-pager>

        <footer>
          <span class="muted">
            ${this._activeCount} ${this._activeCount === 1 ? 'item' : 'items'} left
          </span>

          <div class="filters" role="group" aria-label="Filter todos">
            ${FILTERS.map(
              (name) => html`
                <button
                  class=${name === this._filter ? 'active' : ''}
                  aria-pressed=${name === this._filter}
                  @click=${() => this.#setFilter(name)}
                >
                  ${name}
                </button>
              `,
            )}
          </div>

          <button class="link" ?hidden=${this._completedCount === 0} @click=${() => this.#clearCompleted()}>
            Clear completed
          </button>
        </footer>
      </main>
    `;
  }

  /** Loads a page; the server clamps an out-of-range page to the last one. */
  async #load(page = this._page) {
    this._loading = true;
    await this.#run(async () => {
      const result = await api.list({
        filter: this._filter,
        page,
        size: TodoApp.PAGE_SIZE,
      });
      this._items = result.items;
      this._page = result.page;
      this._totalPages = result.totalPages;
      this._totalItems = result.totalItems;
      this._activeCount = result.activeCount;
      this._completedCount = result.completedCount;
    });
    this._loading = false;
  }

  #setFilter(filter) {
    if (filter === this._filter) return;
    this._filter = filter;
    this.#load(0);
  }

  async #add(title) {
    await this.#run(async () => {
      await api.create(title);
      // New todos append, so follow them to whatever page they landed on.
      const target =
        this._filter === 'completed'
          ? this._page
          : Math.ceil((this._totalItems + 1) / TodoApp.PAGE_SIZE) - 1;
      await this.#load(target);
    });
  }

  async #update(id, changes) {
    await this.#run(async () => {
      await api.update(id, changes);
      await this.#load();
    });
  }

  async #remove(id) {
    await this.#run(async () => {
      await api.remove(id);
      await this.#load();
    });
  }

  async #clearCompleted() {
    await this.#run(async () => {
      await api.clearCompleted();
      await this.#load();
    });
  }

  /** Runs an API call, surfacing any failure in the error banner. */
  async #run(action) {
    this._error = '';
    try {
      await action();
    } catch (error) {
      this._error = error.message || 'Something went wrong.';
    }
  }

  static styles = css`
    :host {
      display: block;
    }
    main {
      max-width: 560px;
      margin: 0 auto;
      padding: 48px 16px 64px;
    }
    h1 {
      margin: 0 0 20px;
      font-size: 28px;
      letter-spacing: -0.02em;
    }
    .card {
      margin-top: 18px;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      overflow: hidden;
    }
    .muted {
      color: var(--muted);
    }
    .card .muted {
      margin: 0;
      padding: 28px 14px;
      text-align: center;
    }
    .error {
      display: flex;
      align-items: center;
      gap: 10px;
      margin: 14px 0 0;
      padding: 10px 14px;
      font-size: 14px;
      color: var(--danger);
      background: color-mix(in srgb, var(--danger) 10%, transparent);
      border-radius: var(--radius);
    }
    footer {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 14px;
      font-size: 14px;
    }
    .filters {
      display: flex;
      gap: 4px;
      margin-left: auto;
    }
    .filters button {
      padding: 4px 10px;
      font: inherit;
      text-transform: capitalize;
      color: var(--muted);
      background: none;
      border: 1px solid transparent;
      border-radius: 999px;
      cursor: pointer;
    }
    .filters button.active {
      color: var(--text);
      border-color: var(--border);
      background: var(--surface);
    }
    .link {
      padding: 4px 6px;
      font: inherit;
      color: var(--muted);
      background: none;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      text-decoration: underline;
    }
    .link:hover {
      color: var(--text);
    }
    /* Keep the footer from reflowing when the button is irrelevant. */
    .link[hidden] {
      display: inline-block;
      visibility: hidden;
    }
  `;
}

customElements.define('todo-app', TodoApp);
