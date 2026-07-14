"use client";

import Link from "next/link";
import {
  ChangeEvent,
  KeyboardEvent,
  ReactNode,
  useEffect,
  useId,
  useRef,
  useState
} from "react";
import {
  EventAutocompleteAuthError,
  EventSummary,
  searchEvents
} from "@/lib/events";

type EventAutocompleteSelectorProps = {
  selectedEvent: EventSummary | null;
  onSelect: (event: EventSummary | null) => void;
};

type SearchStatus = "guidance" | "loading" | "success" | "empty" | "error" | "auth";

export function EventAutocompleteSelector({
  selectedEvent,
  onSelect
}: EventAutocompleteSelectorProps) {
  const inputId = useId();
  const listboxId = useId();
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<EventSummary[]>([]);
  const [status, setStatus] = useState<SearchStatus>("guidance");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const requestIdRef = useRef(0);

  const trimmedQuery = query.trim();
  const activeResult = activeIndex >= 0 ? results[activeIndex] : null;
  const activeDescendant = activeResult ? `${listboxId}-option-${activeResult.id}` : undefined;

  useEffect(() => {
    requestIdRef.current += 1;
    const requestId = requestIdRef.current;

    if (trimmedQuery.length < 3 || selectedEvent?.name === query) {
      return;
    }

    const controller = new AbortController();
    const timeoutId = window.setTimeout(() => {
      setStatus("loading");
      setErrorMessage(null);
      setIsOpen(true);

      searchEvents(trimmedQuery, controller.signal)
        .then((events) => {
          if (requestId !== requestIdRef.current) {
            return;
          }

          setResults(events);
          setStatus(events.length > 0 ? "success" : "empty");
          setIsOpen(true);
          setActiveIndex(events.length > 0 ? 0 : -1);
        })
        .catch((error) => {
          if (controller.signal.aborted || requestId !== requestIdRef.current) {
            return;
          }

          setResults([]);
          setActiveIndex(-1);
          setIsOpen(true);

          if (error instanceof EventAutocompleteAuthError) {
            setStatus("auth");
            setErrorMessage(error.message);
          } else {
            setStatus("error");
            setErrorMessage(error instanceof Error ? error.message : "Could not search events");
          }
        });
    }, 300);

    return () => {
      window.clearTimeout(timeoutId);
      controller.abort();
    };
  }, [query, selectedEvent?.name, trimmedQuery]);

  function handleInputChange(event: ChangeEvent<HTMLInputElement>) {
    const nextQuery = event.target.value;
    setQuery(nextQuery);

    if (selectedEvent) {
      onSelect(null);
    }

    if (nextQuery.trim().length === 0) {
      onSelect(null);
    }

    if (nextQuery.trim().length < 3) {
      setResults([]);
      setStatus("guidance");
      setErrorMessage(null);
      setIsOpen(false);
      setActiveIndex(-1);
    }
  }

  function handleSelect(event: EventSummary) {
    onSelect(event);
    setQuery(event.name);
    setIsOpen(false);
    setActiveIndex(-1);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Escape") {
      setIsOpen(false);
      setActiveIndex(-1);
      return;
    }

    if (!isOpen && (event.key === "ArrowDown" || event.key === "ArrowUp")) {
      setIsOpen(true);
    }

    if (results.length === 0) {
      return;
    }

    if (event.key === "ArrowDown") {
      event.preventDefault();
      setActiveIndex((current) => (current + 1) % results.length);
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();
      setActiveIndex((current) => (current <= 0 ? results.length - 1 : current - 1));
    }

    if (event.key === "Enter" && activeIndex >= 0) {
      event.preventDefault();
      handleSelect(results[activeIndex]);
    }
  }

  return (
    <div className="flex flex-col gap-3">
      <label htmlFor={inputId} className="text-sm font-medium text-slate-700">
        Event
      </label>

      <div className="relative">
        <input
          id={inputId}
          value={query}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          onFocus={() => {
            if (trimmedQuery.length >= 3 && selectedEvent?.name !== query) {
              setIsOpen(true);
            }
          }}
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={isOpen}
          aria-controls={listboxId}
          aria-activedescendant={activeDescendant}
          placeholder="Search existing events"
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900"
        />

        {isOpen ? (
          <div
            id={listboxId}
            role="listbox"
            className="absolute z-10 mt-2 max-h-80 w-full overflow-auto rounded-md border border-slate-200 bg-white shadow-lg"
          >
            {status === "loading" ? <StatusMessage>Searching events...</StatusMessage> : null}

            {status === "success" ? (
              <ul>
                {results.map((event, index) => (
                  <li
                    id={`${listboxId}-option-${event.id}`}
                    key={event.id}
                    role="option"
                    aria-selected={selectedEvent?.id === event.id || index === activeIndex}
                  >
                    <button
                      type="button"
                      onMouseDown={(mouseEvent) => mouseEvent.preventDefault()}
                      onClick={() => handleSelect(event)}
                      className={`flex w-full flex-col gap-1 px-3 py-3 text-left transition ${
                        index === activeIndex ? "bg-slate-100" : "hover:bg-slate-50"
                      }`}
                    >
                      <span className="font-medium text-slate-950">{event.name}</span>
                      <span className="text-sm text-slate-600">
                        {formatEventDate(event.starts_at)}
                      </span>
                      <span className="text-sm text-slate-600">
                        {event.venue}, {event.city}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            ) : null}

            {status === "empty" ? (
              <StatusMessage>
                No matching event was found. Tickets can only be listed for existing TicketPass
                events during MVP.
              </StatusMessage>
            ) : null}

            {status === "error" ? (
              <StatusMessage>{errorMessage ?? "Could not search events"}</StatusMessage>
            ) : null}

            {status === "auth" ? (
              <div className="px-3 py-3 text-sm text-slate-700">
                <p>{errorMessage ?? "Sign in to search and select an event."}</p>
                <Link
                  href="/login"
                  className="mt-2 inline-flex font-medium text-slate-950 underline underline-offset-4"
                >
                  Log in
                </Link>
              </div>
            ) : null}
          </div>
        ) : null}
      </div>

      {status === "guidance" ? (
        <p className="text-sm text-slate-600">Type at least 3 characters to search events.</p>
      ) : null}
    </div>
  );
}

function StatusMessage({ children }: { children: ReactNode }) {
  return <p className="px-3 py-3 text-sm text-slate-700">{children}</p>;
}

export function formatEventDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    timeZoneName: "short"
  }).format(new Date(value));
}
