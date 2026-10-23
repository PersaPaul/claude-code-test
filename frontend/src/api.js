const BASE = '/api/todos';

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(path, {
      headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
      ...options,
    });
  } catch {
    throw new Error('Could not reach the server — is the backend running on :8080?');
  }

  if (!response.ok) {
    throw new Error(await errorMessage(response));
  }
  return response.status === 204 ? null : response.json();
}

/** Spring returns RFC 7807 problem details; fall back to the status line. */
async function errorMessage(response) {
  try {
    const problem = await response.json();
    return problem.detail || problem.title || `Request failed (${response.status})`;
  } catch {
    return `Request failed (${response.status})`;
  }
}

export const api = {
  /** Returns a page envelope: { items, page, size, totalItems, totalPages, activeCount, completedCount }. */
  list: ({ filter = 'all', page = 0, size = 10 } = {}) =>
    request(`${BASE}?${new URLSearchParams({ filter, page, size })}`),

  create: (title) =>
    request(BASE, { method: 'POST', body: JSON.stringify({ title }) }),

  update: (id, changes) =>
    request(`${BASE}/${id}`, { method: 'PATCH', body: JSON.stringify(changes) }),

  remove: (id) => request(`${BASE}/${id}`, { method: 'DELETE' }),

  clearCompleted: () => request(BASE, { method: 'DELETE' }),
};
